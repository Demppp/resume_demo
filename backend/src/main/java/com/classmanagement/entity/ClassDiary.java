package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "class_diary")
@SQLRestriction("deleted = 0")
public class ClassDiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String className;
    private LocalDate diaryDate;

    @Column(name = "author")
    private String recorderName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String diaryContent;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;
    
    @CreationTimestamp
    private LocalDateTime createdTime;
    
    @UpdateTimestamp
    private LocalDateTime updatedTime;
    
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer deleted = 0;
}
