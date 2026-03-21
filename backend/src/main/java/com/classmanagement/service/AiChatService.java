package com.classmanagement.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.classmanagement.config.PromptTemplates;
import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.entity.Student;
import com.classmanagement.repository.AttendanceRepository;
import com.classmanagement.repository.ExamScoreRepository;
import com.classmanagement.repository.StudentRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final StudentRagService studentRagService;
    private final ChatLanguageModel chatLanguageModel;
    private final StudentRepository studentRepository;
    private final ExamScoreRepository examScoreRepository;
    private final AttendanceRepository attendanceRepository;

    // 判断消息是否属于学情查询（走 RAG），否则走普通对话
    private static final List<String> RAG_KEYWORDS = List.of(
            "退步", "进步", "风险", "缺勤", "旷课", "迟到", "成绩下降", "学情",
            "需要关注", "需关注", "重点关注", "干预", "排名靠后", "最差", "最好",
            "找出", "哪些学生", "谁", "分析"
    );

    private boolean isRagQuery(String message) {
        if (message == null) return false;
        return RAG_KEYWORDS.stream().anyMatch(message::contains);
    }

    /**
     * 智能对话入口：
     * - 学情相关问题 → RAG 语义检索 + 大模型分析
     * - 普通问答 → 直接调用 ChatLanguageModel（含数据上下文）
     */
    public Map<String, Object> chat(String message, List<Map<String, String>> history) {
        try {
            if (isRagQuery(message)) {
                log.info("[Chat] 识别为学情查询，走 RAG 链路: {}", message);
                Map<String, Object> ragResult = studentRagService.ragQuery(message);
                if (Boolean.TRUE.equals(ragResult.get("success"))) {
        Map<String, Object> result = new HashMap<>();
            result.put("success", true);
                    result.put("message", ragResult.get("aiAnalysis"));
                    result.put("type", "rag");
                    result.put("retrievedProfiles", ragResult.get("retrievedProfiles"));
                    result.put("retrievedCount", ragResult.get("retrievedCount"));
                    result.put("totalTimeMs", ragResult.get("totalTimeMs"));
        return result;
    }
                // RAG 失败时降级为普通对话
                log.warn("[Chat] RAG 查询失败，降级为普通对话");
            }

            // 普通对话：用 buildStreamPrompt 构建含数据上下文的 Prompt
            log.info("[Chat] 普通对话模式: {}", message);
            String historyJson = history != null ? JSON.toJSONString(history) : "";
            String prompt = buildStreamPrompt(message, historyJson);
            String answer = chatLanguageModel.generate(prompt);
        Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", answer);
            result.put("type", "chat");
            return result;

        } catch (Exception e) {
            log.error("AI 对话失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "AI 响应失败: " + e.getMessage());
        return result;
        }
    }

    public String buildStreamPrompt(String message, String historyJson) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(String.format(PromptTemplates.CHAT_SYSTEM, buildDataContext(message)));
        promptBuilder.append("\n\n");
        if (historyJson != null && !historyJson.isEmpty()) {
            try {
                List<Map<String, String>> history = JSON.parseArray(historyJson, (Class<Map<String, String>>) (Class<?>) Map.class);
                if (!history.isEmpty()) {
                    promptBuilder.append("对话历史：\n");
                    for (Map<String, String> msg : history) {
                        String role = msg.get("role");
                        String content = msg.get("content");
                        promptBuilder.append("user".equals(role) ? "用户: " : "AI: ");
                        promptBuilder.append(content).append("\n");
                    }
                    promptBuilder.append("\n");
                }
            } catch (Exception e) {
                log.warn("解析对话历史失败", e);
            }
        }
        promptBuilder.append("用户当前提问: ").append(message).append("\n\n");
        promptBuilder.append("请基于上下文简洁专业地回答。涉及具体学生时给出数据依据，不要臆造数据。\n");
        return promptBuilder.toString();
    }

    /**
     * 生成单个学生学情分析报告
     * 修复：使用精心构建的 STUDENT_REPORT Prompt 直接调用 ChatLanguageModel，
     * 而非用学生姓名做 RAG 查询（之前的版本丢弃了构建好的 prompt）。
     */
    public Map<String, Object> generateStudentReport(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生不存在");
                return result;
            }

            List<ExamScore> scores = examScoreRepository.findByStudentIdOrderByExamDateAsc(studentId);
            List<Attendance> attendances = attendanceRepository.findByStudentIdAndAttendanceDateAfterOrderByAttendanceDateDesc(
                    studentId, LocalDate.now().minusDays(30)
            );

            String classAvgDesc = "暂无数据";
            String gradeRankDesc = "暂无数据";
            if (!scores.isEmpty()) {
                ExamScore latest = scores.get(scores.size() - 1);
                Optional<Double> avg = examScoreRepository.findAvgScoreByClassNameAndExamName(
                        student.getClassName(), latest.getExamName());
                if (avg.isPresent()) classAvgDesc = String.format("%.1f分", avg.get());
                if (latest.getGradeRank() != null) gradeRankDesc = "第" + latest.getGradeRank() + "名";
                }

            StringBuilder scoresDesc = new StringBuilder();
            for (ExamScore s : scores) {
                scoresDesc.append(s.getExamName()).append("：总分")
                        .append(s.getTotalScore() != null ? s.getTotalScore().toPlainString() : "缺考")
                        .append("分");
                if (s.getChineseScore() != null) {
                    scoresDesc.append(String.format("（语文%s 数学%s 英语%s 综合%s）",
                            s.getChineseScore().toPlainString(),
                            s.getMathScore() != null ? s.getMathScore().toPlainString() : "-",
                            s.getEnglishScore() != null ? s.getEnglishScore().toPlainString() : "-",
                            s.getComprehensiveScore() != null ? s.getComprehensiveScore().toPlainString() : "-"));
                }
                scoresDesc.append("\n");
            }
            if (scoresDesc.isEmpty()) scoresDesc.append("暂无成绩记录\n");

            StringBuilder attendDesc = new StringBuilder();
            if (attendances.isEmpty()) {
                attendDesc.append("近30天无考勤异常记录\n");
            } else {
                long abnormalCount = attendances.stream()
                        .filter(a -> !"正常".equals(a.getAttendanceStatus())).count();
                attendDesc.append(String.format("近30天共%d条记录，异常%d次\n",
                    attendances.size(), abnormalCount));
                for (Attendance a : attendances) {
                    if (!"正常".equals(a.getAttendanceStatus())) {
                        attendDesc.append(String.format("  %s %s%s\n",
                                a.getAttendanceDate(),
                            a.getAttendanceStatus(),
                                a.getReason() != null ? "（" + a.getReason() + "）" : ""));
                    }
                }
            }

            // 使用构建好的 STUDENT_REPORT Prompt 直接调用 ChatLanguageModel
            String prompt = String.format(PromptTemplates.STUDENT_REPORT,
                    student.getStudentName(),
                    student.getClassName(),
                    student.getClassType() != null ? student.getClassType() : "未知",
                    scoresDesc.toString(),
                    attendDesc.toString(),
                    classAvgDesc,
                    gradeRankDesc);

            log.info("[StudentReport] 生成学情报告: studentId={}, name={}", studentId, student.getStudentName());
            String response = chatLanguageModel.generate(prompt);
            String cleanJson = response.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("```$", "")
                    .trim();

            try {
            JSONObject reportJson = JSON.parseObject(cleanJson);
            result.put("success", true);
            result.put("studentName", student.getStudentName());
            result.put("className", student.getClassName());
            result.put("trend", reportJson.getString("trend"));
            result.put("risk_factors", reportJson.getJSONArray("risk_factors"));
            result.put("suggestion", reportJson.getString("suggestion"));
            result.put("urgency", reportJson.getString("urgency"));
            } catch (Exception ex) {
                log.warn("[StudentReport] JSON 解析失败，返回原始文本", ex);
                result.put("success", true);
                result.put("studentName", student.getStudentName());
                result.put("className", student.getClassName());
                result.put("trend", "详见AI分析");
                result.put("risk_factors", List.of("成绩走势需结合近次考试观察", "请关注近期考勤情况"));
                result.put("suggestion", response);
                result.put("urgency", "中");
            }
        } catch (Exception e) {
            log.error("生成学情报告失败", e);
            result.put("success", false);
            result.put("message", "生成报告失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 构建数据上下文摘要（用于普通对话）
     * 优化：不再全量 findAll()，只查统计数据；消息中含学生姓名时才精确查询该学生
     */
    public String buildDataContext(String message) {
        StringBuilder context = new StringBuilder();

        // 只查 count，不全量加载实体
        long studentCount = studentRepository.count();
        context.append("- 学生总数: ").append(studentCount).append("人\n");

        // 班级分布用聚合查询替代 findAll
            try {
            List<Object[]> classCounts = studentRepository.countGroupByClassName();
            if (!classCounts.isEmpty()) {
                context.append("- 班级分布: ");
                for (Object[] row : classCounts) {
                    context.append(row[0]).append("(").append(row[1]).append("人) ");
                    }
                context.append("\n");
                }
            } catch (Exception e) {
            log.warn("查询班级分布失败", e);
    }

        // 如消息提到具体学生姓名，精确查该学生最新成绩
        if (message != null && !message.isEmpty()) {
            try {
                List<String> allNames = studentRepository.findAllStudentNames();
                for (String name : allNames) {
                    if (message.contains(name)) {
                        examScoreRepository.findLatestOneByStudentName(name).ifPresent(s ->
                                context.append("- ").append(name)
                                        .append(" 最新成绩: 总分").append(s.getTotalScore())
                                        .append(" 语文").append(s.getChineseScore())
                                        .append(" 数学").append(s.getMathScore())
                                        .append(" 英语").append(s.getEnglishScore())
                                        .append(" 年级排名第").append(s.getGradeRank()).append("名\n")
                        );
                    break;
                }
            }
            } catch (Exception e) {
                log.warn("查询学生成绩上下文失败", e);
            }
        }

        // 最新考试名称和年级平均分（用聚合，不全量加载）
        try {
            examScoreRepository.findLatestExamNameAndAvg().ifPresent(row -> {
                context.append("- 最新考试: ").append(row[0]).append("\n");
                context.append("- 年级平均分: ").append(
                        String.format("%.1f", ((Number) row[1]).doubleValue())).append("\n");
            });
        } catch (Exception e) {
            log.warn("查询考试统计失败", e);
                    }

        return context.toString();
    }
}
