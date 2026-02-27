package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.ExamScoreDTO;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.mapper.ExamScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamScoreService {
    
    private final ExamScoreMapper examScoreMapper;
    
    public Page<ExamScoreDTO> getExamScoreList(int pageNum, int pageSize, String className, String examName, String studentName) {
        Page<ExamScore> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        
        if (className != null && !className.isEmpty()) {
            wrapper.eq(ExamScore::getClassName, className);
        }
        if (examName != null && !examName.isEmpty()) {
            wrapper.eq(ExamScore::getExamName, examName);
        }
        if (studentName != null && !studentName.isEmpty()) {
            wrapper.like(ExamScore::getStudentName, studentName);
        }
        
        wrapper.orderByDesc(ExamScore::getExamDate);
        
        Page<ExamScore> result = examScoreMapper.selectPage(page, wrapper);
        
        // 转换为DTO并计算成绩变化
        Page<ExamScoreDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<ExamScoreDTO> dtoList = result.getRecords().stream()
                .map(ExamScoreDTO::fromEntity)
                .collect(Collectors.toList());
        
        // 计算成绩变化
        calculateScoreChanges(dtoList);
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
    
    private void calculateScoreChanges(List<ExamScoreDTO> currentScores) {
        if (currentScores == null || currentScores.isEmpty()) {
            return;
        }
        
        // 获取所有考试名称并排序
        LambdaQueryWrapper<ExamScore> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.select(ExamScore::getExamName)
                   .groupBy(ExamScore::getExamName)
                   .orderByAsc(ExamScore::getExamName);
        List<ExamScore> examList = examScoreMapper.selectList(examWrapper);
        List<String> examNames = examList.stream()
                .map(ExamScore::getExamName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // 为每条记录计算成绩变化
        for (ExamScoreDTO score : currentScores) {
            int currentExamIndex = examNames.indexOf(score.getExamName());
            
            // 如果不是第一次考试，查找上一次考试的成绩
            if (currentExamIndex > 0) {
                String previousExamName = examNames.get(currentExamIndex - 1);
                
                // 查询该学生上一次考试的成绩
                LambdaQueryWrapper<ExamScore> prevWrapper = new LambdaQueryWrapper<>();
                prevWrapper.eq(ExamScore::getStudentId, score.getStudentId())
                           .eq(ExamScore::getExamName, previousExamName)
                           .last("LIMIT 1");
                ExamScore prevScore = examScoreMapper.selectOne(prevWrapper);
                
                if (prevScore != null) {
                    BigDecimal change = score.getTotalScore().subtract(prevScore.getTotalScore());
                    score.setScoreChange(change);
                }
            }
        }
    }
    
    public ExamScore addExamScore(ExamScore examScore) {
        // 计算总分
        BigDecimal total = examScore.getChineseScore()
                .add(examScore.getMathScore())
                .add(examScore.getEnglishScore())
                .add(examScore.getComprehensiveScore());
        examScore.setTotalScore(total);
        
        // 预测大学
        String predictedUniversity = predictUniversity(total, examScore.getClassType());
        examScore.setPredictedUniversity(predictedUniversity);
        
        examScoreMapper.insert(examScore);
        
        // 计算排名
        calculateRanks(examScore);
        
        return examScore;
    }
    
    private void calculateRanks(ExamScore examScore) {
        // 计算班级排名
        LambdaQueryWrapper<ExamScore> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(ExamScore::getClassName, examScore.getClassName())
                   .eq(ExamScore::getExamName, examScore.getExamName())
                   .gt(ExamScore::getTotalScore, examScore.getTotalScore());
        long classRank = examScoreMapper.selectCount(classWrapper) + 1;
        examScore.setClassRank((int) classRank);
        
        // 计算年级排名
        LambdaQueryWrapper<ExamScore> gradeWrapper = new LambdaQueryWrapper<>();
        gradeWrapper.eq(ExamScore::getExamName, examScore.getExamName())
                   .eq(ExamScore::getClassType, examScore.getClassType())
                   .gt(ExamScore::getTotalScore, examScore.getTotalScore());
        long gradeRank = examScoreMapper.selectCount(gradeWrapper) + 1;
        examScore.setGradeRank((int) gradeRank);
        
        examScoreMapper.updateById(examScore);
    }
    
    private String predictUniversity(BigDecimal totalScore, String classType) {
        // 基于2013年广东高考分数线预测
        // 理科：一本574分，二本516分
        // 文科：一本594分，二本546分
        
        double score = totalScore.doubleValue();
        
        if ("理科".equals(classType)) {
            if (score >= 650) {
                return "985高校(如中山大学、华南理工大学等)";
            } else if (score >= 600) {
                return "211高校(如暨南大学、华南师范大学等)";
            } else if (score >= 574) {
                return "一本院校";
            } else if (score >= 516) {
                return "二本院校";
            } else if (score >= 400) {
                return "专科院校";
            } else {
                return "需要加强学习";
            }
        } else {
            if (score >= 640) {
                return "985高校(如中山大学、华南理工大学等)";
            } else if (score >= 590) {
                return "211高校(如暨南大学、华南师范大学等)";
            } else if (score >= 594) {
                return "一本院校";
            } else if (score >= 546) {
                return "二本院校";
            } else if (score >= 430) {
                return "专科院校";
            } else {
                return "需要加强学习";
            }
        }
    }
    
    public ExamScore updateExamScore(ExamScore examScore) {
        // 重新计算总分
        BigDecimal total = examScore.getChineseScore()
                .add(examScore.getMathScore())
                .add(examScore.getEnglishScore())
                .add(examScore.getComprehensiveScore());
        examScore.setTotalScore(total);
        
        // 重新预测大学
        String predictedUniversity = predictUniversity(total, examScore.getClassType());
        examScore.setPredictedUniversity(predictedUniversity);
        
        examScoreMapper.updateById(examScore);
        
        // 重新计算排名
        calculateRanks(examScore);
        
        return examScore;
    }
    
    public void deleteExamScore(Long id) {
        examScoreMapper.deleteById(id);
    }
    
    public List<ExamScore> getStudentScores(Long studentId) {
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamScore::getStudentId, studentId)
               .orderByDesc(ExamScore::getExamDate)
               .orderByDesc(ExamScore::getId);
        return examScoreMapper.selectList(wrapper);
    }
}

