package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.dto.WarningDTO;
import com.example.demo.entity.Attendance;
import com.example.demo.entity.ExamScore;
import com.example.demo.entity.Student;
import com.example.demo.mapper.AttendanceMapper;
import com.example.demo.mapper.ExamScoreMapper;
import com.example.demo.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarningService {

    @Autowired
    private ExamScoreMapper examScoreMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private StudentMapper studentMapper;

    public List<WarningDTO> getWarningList() {
        List<WarningDTO> warnings = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);

        // 1. 检查成绩下降超过20%
        warnings.addAll(checkScoreDecline(currentTime));

        // 2. 检查连续旷课
        warnings.addAll(checkAbsence(currentTime));

        // 3. 检查总分低于班级平均值30分
        warnings.addAll(checkLowScore(currentTime));

        // 4. 检查单科持续下降
        warnings.addAll(checkSubjectDecline(currentTime));

        return warnings;
    }

    // 检查成绩下降超过20%
    private List<WarningDTO> checkScoreDecline(String currentTime) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        // 获取所有学生
        List<Student> students = studentMapper.selectList(null);
        
        for (Student student : students) {
            // 获取该学生的所有考试成绩，按考试名称排序
            QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", student.getId())
                   .orderByAsc("exam_name");
            List<ExamScore> scores = examScoreMapper.selectList(wrapper);
            
            if (scores.size() >= 2) {
                ExamScore firstExam = scores.get(0);
                ExamScore secondExam = scores.get(scores.size() - 1);
                
                int firstTotal = firstExam.getChinese() + firstExam.getMath() + 
                                firstExam.getEnglish() + firstExam.getComprehensive();
                int secondTotal = secondExam.getChinese() + secondExam.getMath() + 
                                 secondExam.getEnglish() + secondExam.getComprehensive();
                
                double declineRate = (double)(firstTotal - secondTotal) / firstTotal;
                
                if (declineRate > 0.2) {
                    WarningDTO warning = new WarningDTO();
                    warning.setStudentName(student.getName());
                    warning.setClassName(student.getClassName());
                    warning.setWarningType("成绩下降");
                    warning.setWarningContent(String.format("📉 周考成绩下降超过20%% (从%d分降至%d分，下降%.1f%%)", 
                        firstTotal, secondTotal, declineRate * 100));
                    warning.setWarningLevel("严重");
                    warning.setWarningTime(currentTime);
                    warnings.add(warning);
                }
            }
        }
        
        return warnings;
    }

    // 检查连续旷课
    private List<WarningDTO> checkAbsence(String currentTime) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        // 获取所有学生
        List<Student> students = studentMapper.selectList(null);
        
        for (Student student : students) {
            // 获取该学生最近的考勤记录
            QueryWrapper<Attendance> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", student.getId())
                   .orderByDesc("date")
                   .last("LIMIT 5");
            List<Attendance> attendances = attendanceMapper.selectList(wrapper);
            
            // 检查是否有连续旷课
            int consecutiveAbsence = 0;
            for (Attendance attendance : attendances) {
                if ("旷课".equals(attendance.getStatus())) {
                    consecutiveAbsence++;
                    if (consecutiveAbsence >= 2) {
                        WarningDTO warning = new WarningDTO();
                        warning.setStudentName(student.getName());
                        warning.setClassName(student.getClassName());
                        warning.setWarningType("考勤异常");
                        warning.setWarningContent(String.format("❌ 连续旷课%d天，需要重点关注", consecutiveAbsence));
                        warning.setWarningLevel("严重");
                        warning.setWarningTime(currentTime);
                        warnings.add(warning);
                        break;
                    }
                } else {
                    consecutiveAbsence = 0;
                }
            }
        }
        
        return warnings;
    }

    // 检查总分低于班级平均值30分
    private List<WarningDTO> checkLowScore(String currentTime) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        // 获取最新的考试名称
        QueryWrapper<ExamScore> examWrapper = new QueryWrapper<>();
        examWrapper.orderByDesc("exam_name").last("LIMIT 1");
        ExamScore latestExam = examScoreMapper.selectOne(examWrapper);
        
        if (latestExam == null) {
            return warnings;
        }
        
        String examName = latestExam.getExamName();
        
        // 按班级分组计算平均分
        Map<String, List<ExamScore>> scoresByClass = new HashMap<>();
        QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_name", examName);
        List<ExamScore> allScores = examScoreMapper.selectList(wrapper);
        
        for (ExamScore score : allScores) {
            Student student = studentMapper.selectById(score.getStudentId());
            if (student != null) {
                scoresByClass.computeIfAbsent(student.getClassName(), k -> new ArrayList<>()).add(score);
            }
        }
        
        // 检查每个班级的学生
        for (Map.Entry<String, List<ExamScore>> entry : scoresByClass.entrySet()) {
            String className = entry.getKey();
            List<ExamScore> classScores = entry.getValue();
            
            // 计算班级平均分
            double avgScore = classScores.stream()
                .mapToInt(s -> s.getChinese() + s.getMath() + s.getEnglish() + s.getComprehensive())
                .average()
                .orElse(0);
            
            // 检查低于平均分30分的学生
            for (ExamScore score : classScores) {
                int totalScore = score.getChinese() + score.getMath() + score.getEnglish() + score.getComprehensive();
                if (avgScore - totalScore >= 30) {
                    Student student = studentMapper.selectById(score.getStudentId());
                    if (student != null) {
                        WarningDTO warning = new WarningDTO();
                        warning.setStudentName(student.getName());
                        warning.setClassName(className);
                        warning.setWarningType("成绩偏低");
                        warning.setWarningContent(String.format("📊 总分%d分，低于班级平均值%.1f分", 
                            totalScore, avgScore - totalScore));
                        warning.setWarningLevel("警告");
                        warning.setWarningTime(currentTime);
                        warnings.add(warning);
                    }
                }
            }
        }
        
        return warnings;
    }

    // 检查单科持续下降
    private List<WarningDTO> checkSubjectDecline(String currentTime) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        // 获取所有学生
        List<Student> students = studentMapper.selectList(null);
        
        for (Student student : students) {
            // 获取该学生的所有考试成绩
            QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", student.getId())
                   .orderByAsc("exam_name");
            List<ExamScore> scores = examScoreMapper.selectList(wrapper);
            
            if (scores.size() >= 2) {
                ExamScore firstExam = scores.get(0);
                ExamScore secondExam = scores.get(scores.size() - 1);
                
                // 检查各科是否下降
                String[] subjects = {"语文", "数学", "英语", "文综/理综"};
                int[] firstScores = {firstExam.getChinese(), firstExam.getMath(), 
                                    firstExam.getEnglish(), firstExam.getComprehensive()};
                int[] secondScores = {secondExam.getChinese(), secondExam.getMath(), 
                                     secondExam.getEnglish(), secondExam.getComprehensive()};
                
                for (int i = 0; i < subjects.length; i++) {
                    if (firstScores[i] - secondScores[i] >= 20) {
                        WarningDTO warning = new WarningDTO();
                        warning.setStudentName(student.getName());
                        warning.setClassName(student.getClassName());
                        warning.setWarningType("单科下降");
                        warning.setWarningContent(String.format("📉 %s成绩持续下降 (从%d分降至%d分，下降%d分)", 
                            subjects[i], firstScores[i], secondScores[i], firstScores[i] - secondScores[i]));
                        warning.setWarningLevel("警告");
                        warning.setWarningTime(currentTime);
                        warnings.add(warning);
                    }
                }
            }
        }
        
        return warnings;
    }
}

