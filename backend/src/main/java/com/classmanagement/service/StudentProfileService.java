package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.entity.Student;
import com.classmanagement.mapper.AttendanceMapper;
import com.classmanagement.mapper.ExamScoreMapper;
import com.classmanagement.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentMapper studentMapper;
    private final ExamScoreMapper examScoreMapper;
    private final AttendanceMapper attendanceMapper;

    public Map<String, Object> getStudentProfile(Long studentId) {
        Map<String, Object> profile = new HashMap<>();

        // 基本信息
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return profile;
        }
        profile.put("student", student);

        // 历次考试成绩
        LambdaQueryWrapper<ExamScore> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(ExamScore::getStudentId, studentId);
        scoreWrapper.orderByAsc(ExamScore::getExamDate);
        List<ExamScore> scores = examScoreMapper.selectList(scoreWrapper);
        profile.put("scores", scores);

        // 成绩趋势数据
        List<Map<String, Object>> scoreTrend = new ArrayList<>();
        for (ExamScore s : scores) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("examName", s.getExamName());
            point.put("totalScore", s.getTotalScore());
            point.put("chineseScore", s.getChineseScore());
            point.put("mathScore", s.getMathScore());
            point.put("englishScore", s.getEnglishScore());
            point.put("comprehensiveScore", s.getComprehensiveScore());
            point.put("gradeRank", s.getGradeRank());
            point.put("classRank", s.getClassRank());
            scoreTrend.add(point);
        }
        profile.put("scoreTrend", scoreTrend);

        // 考勤统计
        LambdaQueryWrapper<Attendance> attWrapper = new LambdaQueryWrapper<>();
        attWrapper.eq(Attendance::getStudentId, studentId);
        List<Attendance> attendances = attendanceMapper.selectList(attWrapper);

        Map<String, Long> attendanceStats = attendances.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceStatus, Collectors.counting()));
        profile.put("attendanceStats", attendanceStats);
        profile.put("attendanceRecords", attendances);

        // 最近一次成绩摘要
        if (!scores.isEmpty()) {
            ExamScore latest = scores.get(scores.size() - 1);
            profile.put("latestScore", latest);
        }

        return profile;
    }
}
