package com.classmanagement.repository;

import com.classmanagement.entity.StudentProfileEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileEmbeddingRepository extends JpaRepository<StudentProfileEmbedding, Long> {

    Optional<StudentProfileEmbedding> findByStudentId(Long studentId);

    List<StudentProfileEmbedding> findByRiskLevel(String riskLevel);

    List<StudentProfileEmbedding> findByClassName(String className);

    /**
     * 核心 RAG 检索：使用 pgvector cosine 相似度检索最相关的学生画像
     * 1 - (embedding <=> query_vector) 即为 cosine 相似度
     *
     * @param queryVector 查询向量字符串，格式如 "[0.1, 0.2, ...]"
     * @param topK        返回最相似的 K 条
     */
    @Query(value = """
            SELECT id, student_id, student_name, class_name, class_type,
                   profile_text, latest_total_score, score_trend,
                   abnormal_attendance_count, grade_rank, risk_level, updated_at,
                   1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity
            FROM student_profile_embedding
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(:queryVector AS vector)
            LIMIT :topK
            """,
            nativeQuery = true)
    List<Object[]> findTopKBySimilarity(@Param("queryVector") String queryVector,
                                         @Param("topK") int topK);

    /**
     * Upsert 向量：存在则更新，不存在则插入
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO student_profile_embedding
                (student_id, student_name, class_name, class_type, profile_text, embedding,
                 latest_total_score, score_trend, abnormal_attendance_count, grade_rank, risk_level, updated_at)
            VALUES (:studentId, :studentName, :className, :classType, :profileText,
                    CAST(:embedding AS vector),
                    :latestTotalScore, :scoreTrend, :abnormalCount, :gradeRank, :riskLevel, NOW())
            ON CONFLICT (student_id) DO UPDATE SET
                student_name = EXCLUDED.student_name,
                class_name = EXCLUDED.class_name,
                class_type = EXCLUDED.class_type,
                profile_text = EXCLUDED.profile_text,
                embedding = EXCLUDED.embedding,
                latest_total_score = EXCLUDED.latest_total_score,
                score_trend = EXCLUDED.score_trend,
                abnormal_attendance_count = EXCLUDED.abnormal_attendance_count,
                grade_rank = EXCLUDED.grade_rank,
                risk_level = EXCLUDED.risk_level,
                updated_at = NOW()
            """,
            nativeQuery = true)
    void upsertEmbedding(@Param("studentId") Long studentId,
                          @Param("studentName") String studentName,
                          @Param("className") String className,
                          @Param("classType") String classType,
                          @Param("profileText") String profileText,
                          @Param("embedding") String embedding,
                          @Param("latestTotalScore") java.math.BigDecimal latestTotalScore,
                          @Param("scoreTrend") String scoreTrend,
                          @Param("abnormalCount") Integer abnormalCount,
                          @Param("gradeRank") Integer gradeRank,
                          @Param("riskLevel") String riskLevel);
}

