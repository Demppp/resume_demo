package com.classmanagement.service;

import com.classmanagement.config.PromptTemplates;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生 RAG 查询服务
 *
 * 技术亮点（面试说明点）：
 * 1. 用千问 text-embedding-v3 将自然语言 query 向量化（1024维）
 * 2. 通过 pgvector 余弦距离运算符 <=> 做语义相似度检索
 * 3. 召回最相关的学生学情画像，构建增强 Prompt
 * 4. 调用千问大模型输出专业分析建议，形成完整 RAG 闭环
 *
 * 注意：直接使用 JdbcTemplate 拼接向量字符串执行 native SQL，
 * 避免 JPA EntityManager 对 pgvector CAST 参数绑定的兼容性问题。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentRagService {

    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;
    private final JdbcTemplate jdbcTemplate;

    private static final int DEFAULT_TOP_K = 5;

    public Map<String, Object> ragQuery(String query) {
        return ragQuery(query, DEFAULT_TOP_K);
    }

    public Map<String, Object> ragQuery(String query, int topK) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: 将用户自然语言 query 向量化
            log.info("[RAG] 开始向量化查询: {}", query);
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            String queryVectorStr = toVectorString(queryEmbedding.vector());
            long embedTime = System.currentTimeMillis() - startTime;
            log.info("[RAG] 向量化完成，耗时 {}ms，维度: {}", embedTime, queryEmbedding.vector().length);

            // Step 2: pgvector 相似度检索
            List<Map<String, Object>> retrievedProfiles = searchTopK(queryVectorStr, topK);
            log.info("[RAG] pgvector 检索完成，召回 {} 条结果", retrievedProfiles.size());

            if (retrievedProfiles.isEmpty()) {
                result.put("success", false);
                result.put("message", "暂无足够的学生画像数据，请先执行向量索引构建");
                return result;
            }

            // Step 3: 构建增强 Prompt，调用千问生成分析
            String augmentedPrompt = buildRagPrompt(query, retrievedProfiles);
            log.info("[RAG] 调用千问生成分析报告...");
            String aiAnalysis = chatLanguageModel.generate(augmentedPrompt);
            long totalTime = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("query", query);
            result.put("retrievedCount", retrievedProfiles.size());
            result.put("retrievedProfiles", retrievedProfiles);
            result.put("aiAnalysis", aiAnalysis);
            result.put("totalTimeMs", totalTime);
            result.put("embedTimeMs", embedTime);
            log.info("[RAG] 查询完成，总耗时 {}ms", totalTime);

        } catch (Exception e) {
            log.error("[RAG] 查询失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "RAG 查询失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 使用 JdbcTemplate 直接执行 pgvector 检索 SQL
     *
     * 核心：把向量字符串直接嵌入 SQL，绕过 JPA 参数绑定对 vector 类型的兼容性问题。
     * 使用余弦距离运算符 <=> 进行语义相似度排序。
     */
    private List<Map<String, Object>> searchTopK(String queryVectorStr, int topK) {
        // 对向量字符串做基本安全校验（只允许数字、逗号、小数点、负号、方括号）
        if (!queryVectorStr.matches("[\\[\\]0-9.,E+\\-]+")) {
            throw new IllegalArgumentException("向量格式非法");
        }

        String sql = "SELECT id, student_id, student_name, class_name, class_type, " +
                "profile_text, latest_total_score, score_trend, " +
                "abnormal_attendance_count, grade_rank, risk_level, updated_at, " +
                "1 - (embedding <=> '" + queryVectorStr + "'::vector) AS similarity " +
                "FROM student_profile_embedding " +
                "WHERE embedding IS NOT NULL " +
                "ORDER BY embedding <=> '" + queryVectorStr + "'::vector " +
                "LIMIT " + topK;

        log.debug("[RAG] 执行检索 SQL（向量维度 {}）", queryVectorStr.split(",").length);

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", rs.getLong("id"));
            profile.put("studentId", rs.getLong("student_id"));
            profile.put("studentName", rs.getString("student_name"));
            profile.put("className", rs.getString("class_name"));
            profile.put("classType", rs.getString("class_type"));
            profile.put("profileText", rs.getString("profile_text"));
            profile.put("latestTotalScore", rs.getBigDecimal("latest_total_score"));
            profile.put("scoreTrend", rs.getString("score_trend"));
            profile.put("abnormalAttendanceCount", rs.getInt("abnormal_attendance_count"));
            profile.put("gradeRank", rs.getObject("grade_rank"));
            profile.put("riskLevel", rs.getString("risk_level"));
            profile.put("updatedAt", rs.getString("updated_at"));
            profile.put("similarity", rs.getDouble("similarity"));
            return profile;
        });
    }

    /**
     * 构建 RAG 增强 Prompt
     * 将检索到的学生画像数据注入 Prompt，提升大模型回答的准确性和针对性
     */
    private String buildRagPrompt(String query, List<Map<String, Object>> profiles) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位经验丰富的高三班级管理AI助手。");
        sb.append("背景知识：").append(PromptTemplates.GAOKAO_SCORE_CONTEXT).append("\n\n");
        sb.append("以下是通过语义检索找到的最相关学生画像数据：\n\n");

        for (int i = 0; i < profiles.size(); i++) {
            Map<String, Object> p = profiles.get(i);
            Object totalScore = p.get("latestTotalScore");
            Object gradeRank = p.get("gradeRank");
            String riskLevel = (String) p.get("riskLevel");
            String scoreTrend = (String) p.get("scoreTrend");
            Object abnormal = p.get("abnormalAttendanceCount");

            sb.append("【学生").append(i + 1).append("】\n");
            sb.append("姓名：").append(p.get("studentName")).append("\n");
            sb.append("班级：").append(p.get("className")).append("（").append(p.get("classType")).append("）\n");
            sb.append("最新总分：").append(totalScore != null ? totalScore + "分" : "暂无").append("\n");
            sb.append("年级排名：").append(gradeRank != null ? "第" + gradeRank + "名" : "暂无").append("\n");
            sb.append("成绩趋势：").append(formatTrend(scoreTrend)).append("\n");
            sb.append("近30天缺勤次数：").append(abnormal != null ? abnormal + "次" : "0次").append("\n");
            sb.append("风险等级：").append(formatRiskLevel(riskLevel)).append("\n");
            sb.append("学情摘要：").append(p.get("profileText")).append("\n");
            sb.append("语义相似度：").append(String.format("%.2f%%", ((Number) p.getOrDefault("similarity", 0)).doubleValue() * 100)).append("\n\n");
        }

        sb.append("---\n");
        sb.append("用户问题：").append(query).append("\n\n");
        sb.append("请严格基于以上学生的【最新总分】和【年级排名】数据回答，不要推测，不要与成绩矛盾。给出：\n");
        sb.append("1. 直接回答用户问题（点名具体学生，附上分数依据）\n");
        sb.append("2. 针对性的干预建议（按优先级排序）\n");
        sb.append("3. 需要重点关注的风险信号\n");
        sb.append("回答要简洁专业，突出数据依据。");
        return sb.toString();
    }

    private String formatTrend(String trend) {
        if (trend == null) return "稳定";
        return switch (trend) {
            case "rising" -> "持续上升";
            case "declining" -> "持续下降";
            default -> "基本稳定";
        };
    }

    private String formatRiskLevel(String level) {
        if (level == null) return "未知";
        return switch (level) {
            case "high" -> "高风险";
            case "medium" -> "中风险";
            case "low" -> "低风险";
            default -> level;
        };
    }

    /**
     * 将 float[] 转为 pgvector 接受的字符串格式 "[0.1,0.2,...]"
     */
    private String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
