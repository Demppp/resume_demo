package com.classmanagement.controller;

import com.classmanagement.config.PromptTemplates;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.repository.AttendanceRepository;
import com.classmanagement.repository.ExamScoreRepository;
import com.classmanagement.repository.StudentRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final ChatLanguageModel chatLanguageModel;
    private final ExamScoreRepository examScoreRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    /**
     * 数据大屏 AI 周报播报
     */
    @GetMapping("/weekly-broadcast")
    public Result<Map<String, Object>> weeklyBroadcast() {
        try {
            List<ExamScore> allScores = examScoreRepository.findAll();
            StringBuilder dataDesc = new StringBuilder();
            long studentCount = studentRepository.count();
            dataDesc.append("年级学生总数：").append(studentCount).append("人\n");

            if (!allScores.isEmpty()) {
                String latestExam = allScores.stream()
                    .filter(s -> s.getExamDate() != null)
                    .max(Comparator.comparing(ExamScore::getExamDate))
                        .map(ExamScore::getExamName)
                        .orElse(null);
                if (latestExam != null) {
                    List<ExamScore> latestScores = allScores.stream()
                        .filter(s -> latestExam.equals(s.getExamName()))
                            .toList();
                    OptionalDouble avg = latestScores.stream()
                        .filter(s -> s.getTotalScore() != null)
                            .mapToDouble(s -> s.getTotalScore().doubleValue())
                            .average();
                    dataDesc.append("最近一次考试：").append(latestExam).append("\n");
                    avg.ifPresent(v -> dataDesc.append("年级平均分：").append(String.format("%.1f", v)).append("分\n"));

                    Map<String, Double> classAvg = latestScores.stream()
                        .filter(s -> s.getTotalScore() != null && s.getClassName() != null)
                        .collect(Collectors.groupingBy(ExamScore::getClassName,
                            Collectors.averagingDouble(s -> s.getTotalScore().doubleValue())));
                    if (!classAvg.isEmpty()) {
                        String topClass = classAvg.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                                .map(e -> e.getKey() + "（" + String.format("%.1f", e.getValue()) + "分）")
                                .orElse("无");
                        dataDesc.append("平均分最高班级：").append(topClass).append("\n");
                    }
                }
            }

            long abnormalCount = attendanceRepository.findAll().stream()
                    .filter(a -> !"正常".equals(a.getAttendanceStatus()))
                    .count();
            dataDesc.append("历史考勤异常记录：").append(abnormalCount).append("条\n");

            String prompt = String.format(PromptTemplates.WEEKLY_BROADCAST, dataDesc.toString());
            String broadcast = chatLanguageModel.generate(prompt);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("broadcast", broadcast);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成周报播报失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "生成失败：" + e.getMessage());
            return Result.success(result);
        }
    }
}
