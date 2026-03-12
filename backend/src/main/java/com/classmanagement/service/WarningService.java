package com.classmanagement.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.WarningDTO;
import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.entity.Student;
import com.classmanagement.mapper.AttendanceMapper;
import com.classmanagement.mapper.ExamScoreMapper;
import com.classmanagement.mapper.StudentMapper;
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

    public Page<WarningDTO> getWarningPage(int pageNum, int pageSize) {
        List<WarningDTO> allWarnings = getWarningList();
        
        Page<WarningDTO> page = new Page<>(pageNum, pageSize);
        page.setTotal(allWarnings.size());
        
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allWarnings.size());
        
        if (start < allWarnings.size()) {
            page.setRecords(allWarnings.subList(start, end));
        } else {
            page.setRecords(new ArrayList<>());
        }
        
        return page;
    }

    public List<WarningDTO> getWarningList() {
        List<WarningDTO> warnings = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentTime = LocalDateTime.now().format(formatter);

        // 一次性获取所有需要的数据
        List<Student> students = studentMapper.selectList(null);
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        
        List<ExamScore> allScores = examScoreMapper.selectList(null);
        Map<Long, List<ExamScore>> scoresByStudent = allScores.stream()
                .collect(Collectors.groupingBy(ExamScore::getStudentId));
        
        List<Attendance> allAttendances = attendanceMapper.selectList(null);
        Map<Long, List<Attendance>> attendancesByStudent = allAttendances.stream()
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        // 1. 检查成绩下降超过20%
        warnings.addAll(checkScoreDecline(currentTime, studentMap, scoresByStudent));

        // 2. 检查连续旷课
        warnings.addAll(checkAbsence(currentTime, studentMap, attendancesByStudent));

        // 3. 检查总分低于班级平均值30分
        warnings.addAll(checkLowScore(currentTime, studentMap, allScores));

        // 4. 检查单科持续下降
        warnings.addAll(checkSubjectDecline(currentTime, studentMap, scoresByStudent));

        return warnings;
    }

    private List<WarningDTO> checkScoreDecline(String currentTime, Map<Long, Student> studentMap, 
                                                Map<Long, List<ExamScore>> scoresByStudent) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        for (Map.Entry<Long, List<ExamScore>> entry : scoresByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<ExamScore> scores = entry.getValue();
            
            if (scores.size() >= 2) {
                scores.sort(Comparator.comparing(ExamScore::getExamDate));
                ExamScore firstExam = scores.get(0);
                ExamScore lastExam = scores.get(scores.size() - 1);
                
                double firstTotal = firstExam.getTotalScore().doubleValue();
                double lastTotal = lastExam.getTotalScore().doubleValue();
                double declineRate = (firstTotal - lastTotal) / firstTotal;
                
                if (declineRate > 0.2) {
                    Student student = studentMap.get(studentId);
                    if (student != null) {
                        WarningDTO warning = new WarningDTO();
                        warning.setStudentName(student.getStudentName());
                        warning.setClassName(student.getClassName());
                        warning.setWarningType("成绩下降");
                        warning.setWarningContent(String.format("📉 周考成绩下降超过20%% (从%.0f分降至%.0f分，下降%.1f%%)", 
                            firstTotal, lastTotal, declineRate * 100));
                        warning.setWarningLevel("严重");
                        warning.setWarningTime(currentTime);
                        warnings.add(warning);
                    }
                }
            }
        }
        
        return warnings;
    }

    private List<WarningDTO> checkAbsence(String currentTime, Map<Long, Student> studentMap,
                                          Map<Long, List<Attendance>> attendancesByStudent) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        for (Map.Entry<Long, List<Attendance>> entry : attendancesByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<Attendance> attendances = entry.getValue();
            
            attendances.sort(Comparator.comparing(Attendance::getAttendanceDate).reversed());
            
            int consecutiveAbsence = 0;
            for (Attendance attendance : attendances) {
                if ("旷课".equals(attendance.getAttendanceStatus())) {
                    consecutiveAbsence++;
                } else {
                    break;
                }
            }
            
            if (consecutiveAbsence >= 2) {
                Student student = studentMap.get(studentId);
                if (student != null) {
                    WarningDTO warning = new WarningDTO();
                    warning.setStudentName(student.getStudentName());
                    warning.setClassName(student.getClassName());
                    warning.setWarningType("考勤异常");
                    warning.setWarningContent(String.format("❌ 连续旷课%d天，需要重点关注", consecutiveAbsence));
                    warning.setWarningLevel("严重");
                    warning.setWarningTime(currentTime);
                    warnings.add(warning);
                }
            }
        }
        
        return warnings;
    }

    private List<WarningDTO> checkLowScore(String currentTime, Map<Long, Student> studentMap,
                                           List<ExamScore> allScores) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        // 获取最新考试
        Optional<ExamScore> latestExamOpt = allScores.stream()
                .max(Comparator.comparing(ExamScore::getExamDate));
        
        if (!latestExamOpt.isPresent()) {
            return warnings;
        }
        
        String examName = latestExamOpt.get().getExamName();
        
        // 按班级分组
        Map<String, List<ExamScore>> scoresByClass = allScores.stream()
                .filter(s -> examName.equals(s.getExamName()))
                .collect(Collectors.groupingBy(ExamScore::getClassName));
        
        for (Map.Entry<String, List<ExamScore>> entry : scoresByClass.entrySet()) {
            String className = entry.getKey();
            List<ExamScore> classScores = entry.getValue();
            
            double avgScore = classScores.stream()
                    .mapToDouble(s -> s.getTotalScore().doubleValue())
                    .average()
                    .orElse(0);
            
            for (ExamScore score : classScores) {
                double totalScore = score.getTotalScore().doubleValue();
                if (avgScore - totalScore >= 30) {
                    Student student = studentMap.get(score.getStudentId());
                    if (student != null) {
                        WarningDTO warning = new WarningDTO();
                        warning.setStudentName(student.getStudentName());
                        warning.setClassName(className);
                        warning.setWarningType("成绩偏低");
                        warning.setWarningContent(String.format("📊 总分%.0f分，低于班级平均值%.1f分", 
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

    private List<WarningDTO> checkSubjectDecline(String currentTime, Map<Long, Student> studentMap,
                                                  Map<Long, List<ExamScore>> scoresByStudent) {
        List<WarningDTO> warnings = new ArrayList<>();
        
        for (Map.Entry<Long, List<ExamScore>> entry : scoresByStudent.entrySet()) {
            Long studentId = entry.getKey();
            List<ExamScore> scores = entry.getValue();
            
            if (scores.size() >= 2) {
                scores.sort(Comparator.comparing(ExamScore::getExamDate));
                ExamScore firstExam = scores.get(0);
                ExamScore lastExam = scores.get(scores.size() - 1);
                
                String[] subjects = {"语文", "数学", "英语", "文综/理综"};
                double[] firstScores = {
                    firstExam.getChineseScore().doubleValue(),
                    firstExam.getMathScore().doubleValue(),
                    firstExam.getEnglishScore().doubleValue(),
                    firstExam.getComprehensiveScore().doubleValue()
                };
                double[] lastScores = {
                    lastExam.getChineseScore().doubleValue(),
                    lastExam.getMathScore().doubleValue(),
                    lastExam.getEnglishScore().doubleValue(),
                    lastExam.getComprehensiveScore().doubleValue()
                };
                
                for (int i = 0; i < subjects.length; i++) {
                    if (firstScores[i] - lastScores[i] >= 20) {
                        Student student = studentMap.get(studentId);
                        if (student != null) {
                            WarningDTO warning = new WarningDTO();
                            warning.setStudentName(student.getStudentName());
                            warning.setClassName(student.getClassName());
                            warning.setWarningType("单科下降");
                            warning.setWarningContent(String.format("📉 %s成绩持续下降 (从%.0f分降至%.0f分，下降%.0f分)", 
                                subjects[i], firstScores[i], lastScores[i], firstScores[i] - lastScores[i]));
                            warning.setWarningLevel("警告");
                            warning.setWarningTime(currentTime);
                            warnings.add(warning);
                        }
                    }
                }
            }
        }
        
        return warnings;
    }
}
