package com.classmanagement.repository;

import com.classmanagement.entity.ClassDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassDiaryRepository extends JpaRepository<ClassDiary, Long>, JpaSpecificationExecutor<ClassDiary> {

    List<ClassDiary> findByClassNameOrderByDiaryDateDesc(String className);

    List<ClassDiary> findByDiaryDateBetweenOrderByDiaryDateDesc(LocalDate start, LocalDate end);

    List<ClassDiary> findTop10ByOrderByDiaryDateDesc();
}

