package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.entity.Student;
import com.classmanagement.mapper.ExamScoreMapper;
import com.classmanagement.mapper.StudentMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final ChatLanguageModel chatLanguageModel;
    private final StudentMapper studentMapper;
    private final ExamScoreMapper examScoreMapper;

    public Map<String, Object> chat(String message, List<Map<String, String>> history) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取数据上下文
            String dataContext = buildDataContext(message);

            // 构建prompt
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("你是一个高三年级班级管理系统的AI助手，帮助年级主任分析学生数据。\n\n");
            promptBuilder.append("以下是当前系统的数据摘要：\n");
            promptBuilder.append(dataContext);
            promptBuilder.append("\n\n");

            // 加入历史对话
            if (history != null && !history.isEmpty()) {
                promptBuilder.append("对话历史：\n");
                for (Map<String, String> msg : history) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    promptBuilder.append(role.equals("user") ? "用户: " : "AI: ");
                    promptBuilder.append(content).append("\n");
                }
                promptBuilder.append("\n");
            }

            promptBuilder.append("用户当前问题: ").append(message).append("\n\n");
            promptBuilder.append("请用中文回答，简洁清晰。如果涉及数据分析，请给出具体数字和建议。");

            String response = chatLanguageModel.generate(promptBuilder.toString());

            result.put("success", true);
            result.put("message", response);
            result.put("type", "chat");
        } catch (Exception e) {
            log.error("AI对话失败", e);
            result.put("success", false);
            result.put("message", "AI服务暂时不可用: " + e.getMessage());
        }
        return result;
    }

    private String buildDataContext(String message) {
        StringBuilder context = new StringBuilder();

        // 学生总数
        Long studentCount = studentMapper.selectCount(null);
        context.append("- 学生总数: ").append(studentCount).append("人\n");

        // 班级统计
        List<Student> allStudents = studentMapper.selectList(null);
        Map<String, Long> classCounts = allStudents.stream()
                .collect(Collectors.groupingBy(Student::getClassName, Collectors.counting()));
        context.append("- 班级情况: ");
        classCounts.forEach((k, v) -> context.append(k).append("(").append(v).append("人) "));
        context.append("\n");

        // 如果问题涉及具体学生
        if (message.length() > 0) {
            // 尝试查找提到的学生
            for (Student s : allStudents) {
                if (message.contains(s.getStudentName())) {
                    LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ExamScore::getStudentId, s.getId());
                    wrapper.orderByDesc(ExamScore::getExamDate);
                    List<ExamScore> scores = examScoreMapper.selectList(wrapper);
                    if (!scores.isEmpty()) {
                        ExamScore latest = scores.get(0);
                        context.append("- ").append(s.getStudentName())
                                .append("(").append(s.getClassName()).append(", ").append(s.getClassType()).append(")")
                                .append(" 最近成绩: 总分").append(latest.getTotalScore())
                                .append(" 语文").append(latest.getChineseScore())
                                .append(" 数学").append(latest.getMathScore())
                                .append(" 英语").append(latest.getEnglishScore())
                                .append(" 年级排名第").append(latest.getGradeRank()).append("名\n");
                    }
                    break;
                }
            }
        }

        // 年级平均分
        List<ExamScore> allScores = examScoreMapper.selectList(null);
        if (!allScores.isEmpty()) {
            // 获取最近一次考试名称
            Set<String> examNames = allScores.stream()
                    .map(ExamScore::getExamName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            context.append("- 已有考试: ").append(String.join(", ", examNames)).append("\n");

            OptionalDouble avgScore = allScores.stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(s -> s.getTotalScore().doubleValue())
                    .average();
            if (avgScore.isPresent()) {
                context.append("- 年级总平均分: ").append(String.format("%.1f", avgScore.getAsDouble())).append("\n");
            }
        }

        return context.toString();
    }
}
