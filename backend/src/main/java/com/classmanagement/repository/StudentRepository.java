package com.classmanagement.repository;

import com.classmanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    List<Student> findByClassName(String className);

    List<Student> findByClassType(String classType);

    Optional<Student> findByStudentName(String studentName);

    List<Student> findByStudentNameContaining(String keyword);

    @Query("SELECT DISTINCT s.className FROM Student s")
    List<String> findAllClassNames();

    @Query("SELECT s FROM Student s WHERE (:className IS NULL OR s.className = :className)")
    List<Student> findByClassNameOptional(@Param("className") String className);

    long countByClassName(String className);

    @Query("SELECT s.studentName FROM Student s WHERE s.deleted = 0")
    List<String> findAllStudentNames();

    @Query("SELECT s.className, COUNT(s) FROM Student s WHERE s.deleted = 0 GROUP BY s.className ORDER BY s.className")
    List<Object[]> countGroupByClassName();
}

