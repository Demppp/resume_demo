package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.ExamScore;
import com.example.demo.entity.Student;
import com.example.demo.mapper.ExamScoreMapper;
import com.example.demo.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ExamScoreMapper examScoreMapper;

    @Autowired
    private StudentMapper studentMapper;

    public Map<String, Object> getAnalyticsData(String examName) {
        Map<String, Object> result = new HashMap<>();

        // 1. 班级平均分趋势数据
        result.put("trendData", getClassTrendData());

        // 2. 各科成绩分布数据
        result.put("distributionData", getScoreDistribution(examName));

        // 3. 学生列表（用于排名变化查询）
        result.put("studentList", getStudentNames());

        return result;
    }

    // 班级平均分趋势
    private Map<String, Object> getClassTrendData() {
        Map<String, Object> data = new HashMap<>();

        // 获取所有考试名称
        List<String> exams = examScoreMapper.selectList(null).stream()
            .map(ExamScore::getExamName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        // 获取所有班级
        List<String> classes = studentMapper.selectList(null).stream()
            .map(Student::getClassName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        data.put("exams", exams);
        data.put("classes", classes);

        // 计算每个班级在每次考试的平均分
        List<Map<String, Object>> series = new ArrayList<>();
        for (String className : classes) {
            Map<String, Object> classData = new HashMap<>();
            classData.put("name", className);

            List<Double> avgScores = new ArrayList<>();
            for (String examName : exams) {
                QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
                wrapper.eq("exam_name", examName);
                List<ExamScore> scores = examScoreMapper.selectList(wrapper);

                double avg = scores.stream()
                    .filter(s -> {
                        Student student = studentMapper.selectById(s.getStudentId());
                        return student != null && className.equals(student.getClassName());
                    })
                    .mapToInt(s -> s.getChinese() + s.getMath() + s.getEnglish() + s.getComprehensive())
                    .average()
                    .orElse(0);

                avgScores.add(Math.round(avg * 10) / 10.0);
            }

            classData.put("data", avgScores);
            series.add(classData);
        }

        data.put("series", series);
        return data;
    }

    // 各科成绩分布
    private Map<String, Object> getScoreDistribution(String examName) {
        Map<String, Object> data = new HashMap<>();

        // 分数段
        List<String> ranges = Arrays.asList("0-60", "60-90", "90-110", "110-130", "130-150");
        data.put("ranges", ranges);

        QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_name", examName);
        List<ExamScore> scores = examScoreMapper.selectList(wrapper);

        // 统计各科在各分数段的人数
        data.put("chinese", countByRange(scores.stream().map(ExamScore::getChinese).collect(Collectors.toList())));
        data.put("math", countByRange(scores.stream().map(ExamScore::getMath).collect(Collectors.toList())));
        data.put("english", countByRange(scores.stream().map(ExamScore::getEnglish).collect(Collectors.toList())));
        data.put("comprehensive", countByRange(scores.stream().map(ExamScore::getComprehensive).collect(Collectors.toList())));

        return data;
    }

    private List<Integer> countByRange(List<Integer> scores) {
        List<Integer> counts = new ArrayList<>();
        counts.add((int) scores.stream().filter(s -> s >= 0 && s < 60).count());
        counts.add((int) scores.stream().filter(s -> s >= 60 && s < 90).count());
        counts.add((int) scores.stream().filter(s -> s >= 90 && s < 110).count());
        counts.add((int) scores.stream().filter(s -> s >= 110 && s < 130).count());
        counts.add((int) scores.stream().filter(s -> s >= 130 && s <= 150).count());
        return counts;
    }

    // 获取学生名单
    private List<String> getStudentNames() {
        return studentMapper.selectList(null).stream()
            .map(Student::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    // 学生排名变化趋势
    public Map<String, Object> getRankingTrend(String studentName) {
        Map<String, Object> data = new HashMap<>();

        // 获取学生
        QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
        studentWrapper.eq("name", studentName);
        Student student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            return data;
        }

        // 获取所有考试
        List<String> exams = examScoreMapper.selectList(null).stream()
            .map(ExamScore::getExamName)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        data.put("exams", exams);

        List<Integer> classRankings = new ArrayList<>();
        List<Integer> gradeRankings = new ArrayList<>();

        for (String examName : exams) {
            QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
            wrapper.eq("exam_name", examName);
            List<ExamScore> allScores = examScoreMapper.selectList(wrapper);

            // 找到该学生的成绩
            ExamScore studentScore = allScores.stream()
                .filter(s -> s.getStudentId().equals(student.getId()))
                .findFirst()
                .orElse(null);

            if (studentScore != null) {
                int studentTotal = studentScore.getChinese() + studentScore.getMath() + 
                                  studentScore.getEnglish() + studentScore.getComprehensive();

                // 计算班级排名
                List<ExamScore> classScores = allScores.stream()
                    .filter(s -> {
                        Student st = studentMapper.selectById(s.getStudentId());
                        return st != null && st.getClassName().equals(student.getClassName());
                    })
                    .collect(Collectors.toList());

                int classRank = 1;
                for (ExamScore score : classScores) {
                    int total = score.getChinese() + score.getMath() + score.getEnglish() + score.getComprehensive();
                    if (total > studentTotal) {
                        classRank++;
                    }
                }
                classRankings.add(classRank);

                // 计算年级排名
                int gradeRank = 1;
                for (ExamScore score : allScores) {
                    int total = score.getChinese() + score.getMath() + score.getEnglish() + score.getComprehensive();
                    if (total > studentTotal) {
                        gradeRank++;
                    }
                }
                gradeRankings.add(gradeRank);
            }
        }

        data.put("classRanking", classRankings);
        data.put("gradeRanking", gradeRankings);

        return data;
    }

    // 风险学生名单
    public List<Map<String, Object>> getRiskStudents(String examName) {
        List<Map<String, Object>> riskStudents = new ArrayList<>();

        QueryWrapper<ExamScore> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_name", examName);
        List<ExamScore> scores = examScoreMapper.selectList(wrapper);

        // 按班级分组计算平均分
        Map<String, Double> classAvgMap = new HashMap<>();
        Map<String, List<ExamScore>> scoresByClass = new HashMap<>();

        for (ExamScore score : scores) {
            Student student = studentMapper.selectById(score.getStudentId());
            if (student != null) {
                scoresByClass.computeIfAbsent(student.getClassName(), k -> new ArrayList<>()).add(score);
            }
        }

        for (Map.Entry<String, List<ExamScore>> entry : scoresByClass.entrySet()) {
            double avg = entry.getValue().stream()
                .mapToInt(s -> s.getChinese() + s.getMath() + s.getEnglish() + s.getComprehensive())
                .average()
                .orElse(0);
            classAvgMap.put(entry.getKey(), avg);
        }

        // 识别风险学生
        for (ExamScore score : scores) {
            Student student = studentMapper.selectById(score.getStudentId());
            if (student == null) continue;

            int totalScore = score.getChinese() + score.getMath() + score.getEnglish() + score.getComprehensive();
            double classAvg = classAvgMap.getOrDefault(student.getClassName(), 0.0);

            // 判断风险等级
            String riskLevel = null;
            String riskReason = null;

            if (classAvg - totalScore >= 50) {
                riskLevel = "高风险";
                riskReason = String.format("总分%d分，低于班级平均分%.1f分", totalScore, classAvg - totalScore);
            } else if (classAvg - totalScore >= 30) {
                riskLevel = "中风险";
                riskReason = String.format("总分%d分，低于班级平均分%.1f分", totalScore, classAvg - totalScore);
            } else if (totalScore < 400) {
                riskLevel = "低风险";
                riskReason = String.format("总分%d分，需要加强辅导", totalScore);
            }

            if (riskLevel != null) {
                Map<String, Object> riskStudent = new HashMap<>();
                riskStudent.put("studentName", student.getName());
                riskStudent.put("className", student.getClassName());
                riskStudent.put("riskLevel", riskLevel);
                riskStudent.put("riskReason", riskReason);
                riskStudents.add(riskStudent);
            }
        }

        // 按风险等级排序
        riskStudents.sort((a, b) -> {
            String levelA = (String) a.get("riskLevel");
            String levelB = (String) b.get("riskLevel");
            Map<String, Integer> levelOrder = Map.of("高风险", 1, "中风险", 2, "低风险", 3);
            return levelOrder.get(levelA).compareTo(levelOrder.get(levelB));
        });

        return riskStudents;
    }
}

