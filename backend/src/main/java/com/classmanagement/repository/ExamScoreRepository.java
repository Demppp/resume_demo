package com.classmanagement.repository;

import com.classmanagement.entity.ExamScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScoreRepository extends JpaRepository<ExamScore, Long>, JpaSpecificationExecutor<ExamScore> {

    List<ExamScore> findByStudentIdOrderByExamDateAsc(Long studentId);

    List<ExamScore> findByStudentIdOrderByExamDateDesc(Long studentId);

    List<ExamScore> findByExamName(String examName);

    List<ExamScore> findByClassNameAndExamName(String className, String examName);

    List<ExamScore> findByStudentName(String studentName);

    @Query("SELECT DISTINCT e.examName FROM ExamScore e ORDER BY e.examName")
    List<String> findAllExamNames();

    @Query("SELECT e FROM ExamScore e WHERE e.studentId = :studentId ORDER BY e.examDate DESC")
    List<ExamScore> findLatestByStudentId(@Param("studentId") Long studentId,
                                          org.springframework.data.domain.Pageable pageable);

    @Query("SELECT e FROM ExamScore e WHERE e.studentId = :studentId ORDER BY e.examDate DESC")
    List<ExamScore> findTop3ByStudentId(@Param("studentId") Long studentId,
                                         org.springframework.data.domain.Pageable pageable);

    @Query("SELECT AVG(e.totalScore) FROM ExamScore e WHERE e.className = :className AND e.examName = :examName")
    Optional<Double> findAvgScoreByClassNameAndExamName(@Param("className") String className,
                                                         @Param("examName") String examName);

    @Query(value = "SELECT * FROM exam_score WHERE student_id = :studentId AND deleted = 0 ORDER BY exam_date DESC LIMIT 1",
           nativeQuery = true)
    Optional<ExamScore> findLatestOneByStudentId(@Param("studentId") Long studentId);

    @Query(value = "SELECT * FROM exam_score WHERE student_name = :studentName AND deleted = 0 ORDER BY exam_date DESC LIMIT 1",
           nativeQuery = true)
    Optional<ExamScore> findLatestOneByStudentName(@Param("studentName") String studentName);

    @Query(value = "SELECT exam_name, AVG(total_score) FROM exam_score WHERE deleted = 0 GROUP BY exam_name ORDER BY MAX(exam_date) DESC LIMIT 1",
           nativeQuery = true)
    Optional<Object[]> findLatestExamNameAndAvg();
}

