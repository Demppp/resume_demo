package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生画像向量表
 * 核心 RAG 实体：存储学情文本摘要及其千问 Embedding 向量
 * 用于 pgvector 语义相似度检索
 */
@Data
@Entity
@Table(name = "student_profile_embedding")
public class StudentProfileEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, unique = true)
    private Long studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    private String className;
    private String classType;

    /**
     * 学情文本摘要（自然语言描述）
     * 示例："张三，高三(1)班理科，近3次考试总分580→542→510持续下降，
     *        本月缺勤3次，年级排名第45名较上次下滑22位，风险等级：高"
     * 该文本经千问 text-embedding-v3 转换为 1024 维向量后存入 embedding 字段
     */
    @Column(name = "profile_text", columnDefinition = "TEXT", nullable = false)
    private String profileText;

    /**
     * pgvector 向量字段（1024 维，对应千问 text-embedding-v3）
     * 使用 columnDefinition 声明原生类型，Hibernate 通过 String 映射存取
     * 实际读写通过原生 SQL 完成，此字段仅用于 DDL 描述
     */
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private String embedding;  // 存储为字符串格式 "[0.1,0.2,...]"

    // 冗余字段，方便快速过滤和排序，无需 JOIN
    @Column(name = "latest_total_score")
    private BigDecimal latestTotalScore;

    /** rising | declining | stable */
    @Column(name = "score_trend")
    private String scoreTrend;

    @Column(name = "abnormal_attendance_count")
    private Integer abnormalAttendanceCount = 0;

    @Column(name = "grade_rank")
    private Integer gradeRank;

    /** high | medium | low */
    @Column(name = "risk_level")
    private String riskLevel;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

