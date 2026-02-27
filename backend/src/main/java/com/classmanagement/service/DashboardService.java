package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanagement.dto.AttendanceWarningDTO;
import com.classmanagement.dto.ClassRankingDTO;
import com.classmanagement.dto.DashboardStatsDTO;
import com.classmanagement.dto.SubjectRankingDTO;
import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.mapper.AttendanceMapper;
import com.classmanagement.mapper.ExamScoreMapper;
import com.classmanagement.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final StudentMapper studentMapper;
    private final ExamScoreMapper examScoreMapper;
    private final AttendanceMapper attendanceMapper;
    
    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 学生总数
        stats.setStudentCount(studentMapper.selectCount(null).intValue());
        stats.setClassCount(6);
        
        // 获取最近一次考试的成绩
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExamScore::getExamDate).last("LIMIT 1");
        ExamScore latestExam = examScoreMapper.selectOne(wrapper);
        
        if (latestExam != null) {
            // 获取该次考试的所有成绩
            LambdaQueryWrapper<ExamScore> examWrapper = new LambdaQueryWrapper<>();
            examWrapper.eq(ExamScore::getExamName, latestExam.getExamName());
            List<ExamScore> scores = examScoreMapper.selectList(examWrapper);
            
            // 计算平均分
            double avgScore = scores.stream()
                    .mapToDouble(s -> s.getTotalScore().doubleValue())
                    .average()
                    .orElse(0.0);
            stats.setAvgScore(String.format("%.1f", avgScore));
            
            // 优秀学生数（600分以上）
            long excellentCount = scores.stream()
                    .filter(s -> s.getTotalScore().doubleValue() >= 600)
                    .count();
            stats.setExcellentCount((int) excellentCount);
        } else {
            stats.setAvgScore("0");
            stats.setExcellentCount(0);
        }
        
        return stats;
    }
    
    public List<ClassRankingDTO> getClassRankings() {
        // 获取最近一次考试
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExamScore::getExamDate).last("LIMIT 1");
        ExamScore latestExam = examScoreMapper.selectOne(wrapper);
        
        if (latestExam == null) {
            return new ArrayList<>();
        }
        
        // 获取该次考试的所有成绩
        LambdaQueryWrapper<ExamScore> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.eq(ExamScore::getExamName, latestExam.getExamName());
        List<ExamScore> scores = examScoreMapper.selectList(examWrapper);
        
        // 按班级分组计算平均分
        Map<String, List<ExamScore>> classScores = scores.stream()
                .collect(Collectors.groupingBy(ExamScore::getClassName));
        
        List<ClassRankingDTO> rankings = new ArrayList<>();
        for (Map.Entry<String, List<ExamScore>> entry : classScores.entrySet()) {
            ClassRankingDTO dto = new ClassRankingDTO();
            dto.setClassName(entry.getKey());
            dto.setStudentCount(entry.getValue().size());
            
            if (!entry.getValue().isEmpty()) {
                dto.setClassType(entry.getValue().get(0).getClassType());
                double avg = entry.getValue().stream()
                        .mapToDouble(s -> s.getTotalScore().doubleValue())
                        .average()
                        .orElse(0.0);
                dto.setAvgScore(String.format("%.1f", avg));
            }
            
            rankings.add(dto);
        }
        
        // 按平均分排序
        rankings.sort((a, b) -> Double.compare(
                Double.parseDouble(b.getAvgScore()),
                Double.parseDouble(a.getAvgScore())
        ));
        
        return rankings;
    }
    
    public List<ExamScore> getTopStudents() {
        // 获取最近一次考试
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExamScore::getExamDate).last("LIMIT 1");
        ExamScore latestExam = examScoreMapper.selectOne(wrapper);
        
        if (latestExam == null) {
            return new ArrayList<>();
        }
        
        // 获取该次考试年级前10名
        LambdaQueryWrapper<ExamScore> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(ExamScore::getExamName, latestExam.getExamName())
                .orderByDesc(ExamScore::getTotalScore)
                .last("LIMIT 10");
        
        return examScoreMapper.selectList(topWrapper);
    }
    
    public List<SubjectRankingDTO> getSubjectRankings(String subject) {
        // 获取最近一次考试
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExamScore::getExamDate).last("LIMIT 1");
        ExamScore latestExam = examScoreMapper.selectOne(wrapper);
        
        if (latestExam == null) {
            return new ArrayList<>();
        }
        
        // 获取该次考试的所有成绩
        LambdaQueryWrapper<ExamScore> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.eq(ExamScore::getExamName, latestExam.getExamName());
        List<ExamScore> scores = examScoreMapper.selectList(examWrapper);
        
        // 根据科目排序
        List<SubjectRankingDTO> rankings = new ArrayList<>();
        for (ExamScore score : scores) {
            SubjectRankingDTO dto = new SubjectRankingDTO();
            dto.setStudentName(score.getStudentName());
            dto.setClassName(score.getClassName());
            dto.setStudentId(score.getStudentId());
            
            BigDecimal subjectScore = null;
            switch (subject) {
                case "chinese":
                    subjectScore = score.getChineseScore();
                    break;
                case "math":
                    subjectScore = score.getMathScore();
                    break;
                case "english":
                    subjectScore = score.getEnglishScore();
                    break;
                case "comprehensive":
                    subjectScore = score.getComprehensiveScore();
                    break;
            }
            
            if (subjectScore != null) {
                dto.setScore(subjectScore.toString());
                rankings.add(dto);
            }
        }
        
        // 按分数排序，取前10名
        rankings.sort((a, b) -> Double.compare(
                Double.parseDouble(b.getScore()),
                Double.parseDouble(a.getScore())
        ));
        
        return rankings.stream().limit(10).collect(Collectors.toList());
    }
    
    public List<AttendanceWarningDTO> getAttendanceWarnings() {
        // 获取最近7天的考勤记录
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Attendance::getAttendanceDate, sevenDaysAgo)
                .ne(Attendance::getAttendanceStatus, "正常");
        
        List<Attendance> attendances = attendanceMapper.selectList(wrapper);
        
        // 按学生分组统计异常次数
        Map<Long, List<Attendance>> studentAttendances = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getStudentId));
        
        List<AttendanceWarningDTO> warnings = new ArrayList<>();
        for (Map.Entry<Long, List<Attendance>> entry : studentAttendances.entrySet()) {
            if (entry.getValue().size() >= 2) { // 异常次数>=2次才预警
                AttendanceWarningDTO dto = new AttendanceWarningDTO();
                dto.setStudentId(entry.getKey());
                dto.setStudentName(entry.getValue().get(0).getStudentName());
                dto.setClassName(entry.getValue().get(0).getClassName());
                dto.setAbnormalCount(entry.getValue().size());
                
                // 获取最近一次的状态
                entry.getValue().sort((a, b) -> b.getAttendanceDate().compareTo(a.getAttendanceDate()));
                dto.setLatestStatus(entry.getValue().get(0).getAttendanceStatus());
                
                warnings.add(dto);
            }
        }
        
        // 按异常次数排序
        warnings.sort((a, b) -> b.getAbnormalCount().compareTo(a.getAbnormalCount()));
        
        return warnings.stream().limit(10).collect(Collectors.toList());
    }
}

