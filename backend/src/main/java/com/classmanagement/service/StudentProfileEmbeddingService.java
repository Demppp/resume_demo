package com.classmanagement.service;

import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.entity.Student;
import com.classmanagement.repository.AttendanceRepository;
import com.classmanagement.repository.ExamScoreRepository;
import com.classmanagement.repository.StudentProfileEmbeddingRepository;
import com.classmanagement.repository.StudentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生画像 Embedding 服务
 *
 * 核心职责：
 * 1. 将学生的多维度学情数据（成绩趋势、出勤、排名）转为自然语言文本
 * 2. 调用千问 text-embedding-v3 生成 1024 维向量
 * 3. 通过 UPSERT 写入 student_profile_embedding 表（pgvector 存储）
 *
 * 面试说明点：
 * - 向量化的是"语义摘要"而非原始数字，提升语义检索的准确性
 * - 使用 UPSERT 避免重复写入，保持向量索引稳定
 * - @Async 异步执行，不阻塞主业务流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileEmbeddingService {

    private final StudentRepository studentRepository;
    private final ExamScoreRepository examScoreRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentProfileEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;

    /**
     * 为单个学生生成并存储学情画像向量
     */
    @Async
    public void buildAndSaveEmbedding(Long studentId) {
        try {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) return;

            // 1. 收集学情数据
            List<ExamScore> scores = examScoreRepository
                    .findLatestByStudentId(studentId, PageRequest.of(0, 3));
            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            long abnormalCount = attendanceRepository
                    .countAbnormalByStudentIdSince(studentId, thirtyDaysAgo);

            // 2. 构建学情文本摘要
            String profileText = buildProfileText(student, scores, abnormalCount);

            // 3. 计算统计字段
            BigDecimal latestScore = scores.isEmpty() ? null : scores.get(0).getTotalScore();
            String scoreTrend = calcScoreTrend(scores);
            Integer gradeRank = scores.isEmpty() ? null : scores.get(0).getGradeRank();
            String riskLevel = calcRiskLevel(scores, abnormalCount);

            // 4. 调用千问 Embedding API 生成向量
            Embedding embedding = embeddingModel.embed(profileText).content();
            String vectorStr = toVectorString(embedding.vector());

            // 5. UPSERT 写入 pgvector
            embeddingRepository.upsertEmbedding(
                    studentId,
                    student.getStudentName(),
                    student.getClassName(),
                    student.getClassType(),
                    profileText,
                    vectorStr,
                    latestScore,
                    scoreTrend,
                    (int) abnormalCount,
                    gradeRank,
                    riskLevel
            );

            log.info("[RAG] 学生 {} 画像向量写入成功，风险等级: {}", student.getStudentName(), riskLevel);
        } catch (Exception e) {
            log.error("[RAG] 学生 {} 画像向量生成失败: {}", studentId, e.getMessage(), e);
        }
    }

    /**
     * 全量重建所有学生的画像向量（手动触发或定时任务）
     */
    public void rebuildAllEmbeddings() {
        List<Student> students = studentRepository.findAll();
        log.info("[RAG] 开始全量重建向量索引，共 {} 名学生", students.size());
        for (Student student : students) {
            buildAndSaveEmbedding(student.getId());
        }
        log.info("[RAG] 全量重建任务已提交（异步执行）");
    }

    /**
     * 构建学情文本摘要
     * 这是 RAG 效果的关键：文本质量决定检索准确性
     */
    private String buildProfileText(Student student, List<ExamScore> scores, long abnormalAttendance) {
        StringBuilder sb = new StringBuilder();
        sb.append(student.getStudentName()).append("，")
          .append(student.getClassName()).append("，")
          .append(student.getClassType() != null ? student.getClassType() : "未知科型").append("。");

        if (scores.isEmpty()) {
            sb.append("暂无考试记录。");
        } else {
            sb.append("近").append(scores.size()).append("次考试总分：");
            String scoreSummary = scores.stream()
                    .map(s -> s.getTotalScore() != null ? s.getTotalScore().toPlainString() : "缺考")
                    .collect(Collectors.joining("→"));
            sb.append(scoreSummary).append("。");

            ExamScore latest = scores.get(0);
            if (latest.getGradeRank() != null) {
                sb.append("当前年级排名第").append(latest.getGradeRank()).append("名。");
            }
            if (latest.getScoreChange() != null) {
                int change = latest.getScoreChange();
                if (change < -20) {
                    sb.append("较上次考试下滑").append(Math.abs(change)).append("分，退步明显。");
                } else if (change > 20) {
                    sb.append("较上次考试提升").append(change).append("分，进步显著。");
                }
            }
            String trend = calcScoreTrend(scores);
            if ("declining".equals(trend)) sb.append("成绩呈持续下降趋势。");
            else if ("rising".equals(trend)) sb.append("成绩呈持续上升趋势。");
            else sb.append("成绩相对稳定。");
        }

        if (abnormalAttendance == 0) {
            sb.append("近30天出勤正常。");
        } else if (abnormalAttendance >= 5) {
            sb.append("近30天缺勤").append(abnormalAttendance).append("次，出勤问题严重，需重点关注。");
        } else {
            sb.append("近30天存在").append(abnormalAttendance).append("次缺勤记录。");
        }

        String riskLevel = calcRiskLevel(scores, abnormalAttendance);
        sb.append("综合风险等级：").append(
                "high".equals(riskLevel) ? "高" : "medium".equals(riskLevel) ? "中" : "低"
        ).append("。");

        return sb.toString();
    }

    /**
     * 计算成绩趋势
     * 比较最新两次考试：下降超15分为 declining，上升超15分为 rising
     */
    private String calcScoreTrend(List<ExamScore> scores) {
        if (scores.size() < 2) return "stable";
        BigDecimal latest = scores.get(0).getTotalScore();
        BigDecimal prev = scores.get(1).getTotalScore();
        if (latest == null || prev == null) return "stable";
        int diff = latest.subtract(prev).intValue();
        if (diff < -15) return "declining";
        if (diff > 15) return "rising";
        return "stable";
    }

    /**
     * 计算风险等级
     * high：成绩持续下降 且/或 缺勤严重
     * medium：轻微下降 或 少量缺勤
     * low：正常
     */
    private String calcRiskLevel(List<ExamScore> scores, long abnormalCount) {
        boolean decliningTrend = "declining".equals(calcScoreTrend(scores));
        boolean seriousAbsence = abnormalCount >= 5;
        boolean mildAbsence = abnormalCount >= 2;

        if (decliningTrend && seriousAbsence) return "high";
        if (decliningTrend || seriousAbsence) return "high";
        if (mildAbsence) return "medium";
        return "low";
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

