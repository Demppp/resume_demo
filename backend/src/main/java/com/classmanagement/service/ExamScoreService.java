package com.classmanagement.service;

import com.classmanagement.dto.ExamScoreDTO;
import com.classmanagement.dto.PageResult;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.repository.ExamScoreRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamScoreService {
    
    private final ExamScoreRepository examScoreRepository;
    private final StudentProfileEmbeddingService studentProfileEmbeddingService;

    public PageResult<ExamScoreDTO> getExamScoreList(int pageNum, int pageSize, String className, String examName, String studentName, BigDecimal minScore, BigDecimal maxScore) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "examDate"));
        Specification<ExamScore> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (className != null && !className.isBlank()) predicates.add(cb.equal(root.get("className"), className));
            if (examName != null && !examName.isBlank()) predicates.add(cb.equal(root.get("examName"), examName));
            if (studentName != null && !studentName.isBlank()) predicates.add(cb.like(root.get("studentName"), "%" + studentName + "%"));
            if (minScore != null) predicates.add(cb.greaterThanOrEqualTo(root.get("totalScore"), minScore));
            if (maxScore != null) predicates.add(cb.lessThanOrEqualTo(root.get("totalScore"), maxScore));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ExamScore> page = examScoreRepository.findAll(spec, pageable);
        List<ExamScoreDTO> dtoList = page.getContent().stream().map(ExamScoreDTO::fromEntity).collect(Collectors.toList());
        calculateScoreChanges(dtoList);
        return new PageResult<>(dtoList, page.getTotalElements(), page.getNumber() + 1, page.getSize(), page.getTotalPages());
    }
    
    public Map<String, Object> getExamStats(String className, String examName, String studentName, BigDecimal minScore, BigDecimal maxScore) {
        List<ExamScore> allRecords = examScoreRepository.findAll();
        List<ExamScore> filtered = allRecords.stream().filter(e ->
                (className == null || className.isBlank() || className.equals(e.getClassName())) &&
                (examName == null || examName.isBlank() || examName.equals(e.getExamName())) &&
                (studentName == null || studentName.isBlank() || (e.getStudentName() != null && e.getStudentName().contains(studentName)))
        ).toList();
        
        Map<String, Object> stats = new HashMap<>();
        long distinctStudents = filtered.stream().map(ExamScore::getStudentId).filter(Objects::nonNull).distinct().count();
        stats.put("totalStudents", distinctStudents);
        
        Map<Long, ExamScore> latestScoreByStudent = new HashMap<>();
        for (ExamScore record : filtered) {
            Long sid = record.getStudentId();
            if (sid == null) continue;
            if (!latestScoreByStudent.containsKey(sid) || 
                    (record.getExamDate() != null && latestScoreByStudent.get(sid).getExamDate() != null && record.getExamDate().isAfter(latestScoreByStudent.get(sid).getExamDate()))) {
                latestScoreByStudent.put(sid, record);
            }
        }
        
        List<ExamScore> latestScores = new ArrayList<>(latestScoreByStudent.values());
        long excellentCount = latestScores.stream().filter(s -> s.getTotalScore() != null && s.getTotalScore().compareTo(new BigDecimal("600")) >= 0).count();
        long needAttentionCount = latestScores.stream().filter(s -> s.getTotalScore() != null && s.getTotalScore().compareTo(new BigDecimal("500")) < 0).count();
        stats.put("excellentCount", excellentCount);
        stats.put("needAttentionCount", needAttentionCount);
        
        BigDecimal avg = latestScores.stream().filter(s -> s.getTotalScore() != null).map(ExamScore::getTotalScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!latestScores.isEmpty()) avg = avg.divide(new BigDecimal(latestScores.size()), 1, RoundingMode.HALF_UP);
        stats.put("averageScore", avg);
        return stats;
    }
    
    private void calculateScoreChanges(List<ExamScoreDTO> currentScores) {
        if (currentScores == null || currentScores.isEmpty()) return;
        List<String> examNames = examScoreRepository.findAllExamNames();
        for (ExamScoreDTO score : currentScores) {
            int currentExamIndex = examNames.indexOf(score.getExamName());
            if (currentExamIndex > 0) {
                String previousExamName = examNames.get(currentExamIndex - 1);
                List<ExamScore> previous = examScoreRepository.findByStudentIdOrderByExamDateDesc(score.getStudentId())
                        .stream().filter(e -> previousExamName.equals(e.getExamName())).limit(1).toList();
                if (!previous.isEmpty() && previous.get(0).getTotalScore() != null && score.getTotalScore() != null) {
                    score.setScoreChange(score.getTotalScore().subtract(previous.get(0).getTotalScore()).intValue());
                }
            }
        }
    }
    
    public ExamScore addExamScore(ExamScore examScore) {
        fillScoreFields(examScore);
        ExamScore saved = examScoreRepository.save(examScore);
        calculateRanks(saved);
        if (saved.getStudentId() != null) studentProfileEmbeddingService.buildAndSaveEmbedding(saved.getStudentId());
        return saved;
    }

    public ExamScore updateExamScore(ExamScore examScore) {
        fillScoreFields(examScore);
        ExamScore saved = examScoreRepository.save(examScore);
        calculateRanks(saved);
        if (saved.getStudentId() != null) studentProfileEmbeddingService.buildAndSaveEmbedding(saved.getStudentId());
        return saved;
    }

    public void deleteExamScore(Long id) {
        ExamScore score = examScoreRepository.findById(id).orElse(null);
        if (score == null) return;
        score.setDeleted(1);
        examScoreRepository.save(score);
        if (score.getStudentId() != null) studentProfileEmbeddingService.buildAndSaveEmbedding(score.getStudentId());
    }

    public List<ExamScore> getStudentScores(Long studentId) {
        return examScoreRepository.findByStudentIdOrderByExamDateDesc(studentId);
    }

    private void fillScoreFields(ExamScore examScore) {
        BigDecimal total = safe(examScore.getChineseScore())
                .add(safe(examScore.getMathScore()))
                .add(safe(examScore.getEnglishScore()))
                .add(safe(examScore.getComprehensiveScore()));
        examScore.setTotalScore(total);
        examScore.setPredictedUniversity(predictUniversity(total, examScore.getClassType()));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    
    private void calculateRanks(ExamScore examScore) {
        List<ExamScore> sameClass = examScoreRepository.findByClassNameAndExamName(examScore.getClassName(), examScore.getExamName());
        long classRank = sameClass.stream().filter(s -> s.getTotalScore() != null && examScore.getTotalScore() != null && s.getTotalScore().compareTo(examScore.getTotalScore()) > 0).count() + 1;
        examScore.setClassRank((int) classRank);
        
        List<ExamScore> sameExam = examScoreRepository.findByExamName(examScore.getExamName()).stream()
                .filter(s -> Objects.equals(s.getClassType(), examScore.getClassType()))
                .toList();
        long gradeRank = sameExam.stream().filter(s -> s.getTotalScore() != null && examScore.getTotalScore() != null && s.getTotalScore().compareTo(examScore.getTotalScore()) > 0).count() + 1;
        examScore.setGradeRank((int) gradeRank);
        examScoreRepository.save(examScore);
    }
    
    private String predictUniversity(BigDecimal totalScore, String classType) {
        double score = totalScore.doubleValue();
        if ("理科".equals(classType)) {
            if (score >= 650) return "985高校(如中山大学、华南理工大学等)";
            if (score >= 600) return "211高校(如暨南大学、华南师范大学等)";
            if (score >= 574) return "一本院校";
            if (score >= 516) return "二本院校";
            if (score >= 400) return "专科院校";
                return "需要加强学习";
            }
        if (score >= 640) return "985高校(如中山大学、华南理工大学等)";
        if (score >= 590) return "211高校(如暨南大学、华南师范大学等)";
        if (score >= 594) return "一本院校";
        if (score >= 546) return "二本院校";
        if (score >= 430) return "专科院校";
                return "需要加强学习";
            }
        }
