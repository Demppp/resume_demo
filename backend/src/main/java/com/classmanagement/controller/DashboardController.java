package com.classmanagement.controller;

import com.classmanagement.repository.AttendanceRepository;
import com.classmanagement.repository.ExamScoreRepository;
import com.classmanagement.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StudentRepository studentRepository;
    private final ExamScoreRepository examScoreRepository;
    private final AttendanceRepository attendanceRepository;

    /** 顶部统计卡片 */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        long studentCount = studentRepository.count();
        List<String> classNames = studentRepository.findAllClassNames();
        long classCount = classNames.size();

        // 取最新一次考试的年级平均分
        List<String> examNames = examScoreRepository.findAllExamNames();
        double avgScore = 0;
        long excellentCount = 0;
        if (!examNames.isEmpty()) {
            String latestExam = examNames.get(examNames.size() - 1);
            var scores = examScoreRepository.findByExamName(latestExam);
            avgScore = scores.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(s -> s.getTotalScore().doubleValue())
                    .average().orElse(0);
            avgScore = Math.round(avgScore * 10.0) / 10.0;
            excellentCount = scores.stream()
                    .filter(s -> s.getTotalScore() != null && s.getTotalScore().doubleValue() >= 600)
                    .count();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studentCount", studentCount);
        data.put("classCount", classCount);
        data.put("avgScore", avgScore);
        data.put("excellentCount", excellentCount);
        return ok(data);
    }

    /** 班级排名 */
    @GetMapping("/class-rankings")
    public Map<String, Object> classRankings() {
        List<String> examNames = examScoreRepository.findAllExamNames();
        if (examNames.isEmpty()) return ok(List.of());
        String latestExam = examNames.get(examNames.size() - 1);

        List<String> classNames = studentRepository.findAllClassNames();
        List<Map<String, Object>> rankings = new ArrayList<>();
        for (String cls : classNames) {
            var scores = examScoreRepository.findByClassNameAndExamName(cls, latestExam);
            if (scores.isEmpty()) continue;
            double avg = scores.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(s -> s.getTotalScore().doubleValue())
                    .average().orElse(0);
            // 科类从学生表取
            var students = studentRepository.findByClassName(cls);
            String classType = students.isEmpty() ? "" : students.get(0).getClassType();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("className", cls);
            row.put("classType", classType);
            row.put("avgScore", Math.round(avg * 10.0) / 10.0);
            row.put("studentCount", scores.size());
            rankings.add(row);
        }
        rankings.sort((a, b) -> Double.compare(
                ((Number) b.get("avgScore")).doubleValue(),
                ((Number) a.get("avgScore")).doubleValue()));
        return ok(rankings);
    }

    /** 年级前10名 */
    @GetMapping("/top-students")
    public Map<String, Object> topStudents() {
        List<String> examNames = examScoreRepository.findAllExamNames();
        if (examNames.isEmpty()) return ok(List.of());
        String latestExam = examNames.get(examNames.size() - 1);
        var scores = examScoreRepository.findByExamName(latestExam);
        var top = scores.stream()
                .filter(s -> s.getTotalScore() != null)
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .limit(10)
                .map(s -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("studentId", s.getStudentId());
                    row.put("studentName", s.getStudentName());
                    row.put("className", s.getClassName());
                    row.put("totalScore", s.getTotalScore());
                    return row;
                })
                .collect(Collectors.toList());
        return ok(top);
    }

    /** 考勤预警（全部异常记录，按异常次数倒序）*/
    @GetMapping("/attendance-warnings")
    public Map<String, Object> attendanceWarnings() {
        var records = attendanceRepository.findAllAbnormal();

        // 按学生聚合
        Map<Long, Map<String, Object>> map = new LinkedHashMap<>();
        for (var r : records) {
            map.computeIfAbsent(r.getStudentId(), id -> {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentId", id);
                row.put("studentName", r.getStudentName());
                row.put("className", r.getClassName());
                row.put("abnormalCount", 0);
                row.put("latestStatus", r.getAttendanceStatus());
                return row;
            });
            Map<String, Object> row = map.get(r.getStudentId());
            row.put("abnormalCount", (int) row.get("abnormalCount") + 1);
            row.put("latestStatus", r.getAttendanceStatus());
        }
        var result = new ArrayList<>(map.values());
        result.sort((a, b) -> Integer.compare(
                (int) b.get("abnormalCount"), (int) a.get("abnormalCount")));
        return ok(result.stream().limit(10).collect(Collectors.toList()));
    }

    /** 单科排名 */
    @GetMapping("/subject-rankings")
    public Map<String, Object> subjectRankings(@RequestParam(defaultValue = "chinese") String subject) {
        List<String> examNames = examScoreRepository.findAllExamNames();
        if (examNames.isEmpty()) return ok(List.of());
        String latestExam = examNames.get(examNames.size() - 1);
        var scores = examScoreRepository.findByExamName(latestExam);
        var result = scores.stream()
                .map(s -> {
                    Number score = switch (subject) {
                        case "math" -> s.getMathScore();
                        case "english" -> s.getEnglishScore();
                        case "comprehensive" -> s.getComprehensiveScore();
                        default -> s.getChineseScore();
                    };
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("studentName", s.getStudentName());
                    row.put("className", s.getClassName());
                    row.put("score", score);
                    return row;
                })
                .filter(r -> r.get("score") != null)
                .sorted((a, b) -> Double.compare(
                        ((Number) b.get("score")).doubleValue(),
                        ((Number) a.get("score")).doubleValue()))
                .limit(10)
                .collect(Collectors.toList());
        return ok(result);
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("code", 200);
        res.put("message", "操作成功");
        res.put("data", data);
        return res;
    }
}

