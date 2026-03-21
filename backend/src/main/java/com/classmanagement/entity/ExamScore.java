package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exam_score")
@SQLRestriction("deleted = 0")
public class ExamScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long studentId;
    private String studentName;
    private String className;
    private String classType;
    private String examName;
    private LocalDate examDate;
    private BigDecimal chineseScore;
    private BigDecimal mathScore;
    private BigDecimal englishScore;
    private BigDecimal comprehensiveScore;
    
    // 理科
    private BigDecimal physicsScore;
    private BigDecimal chemistryScore;
    private BigDecimal biologyScore;
    
    // 文科
    private BigDecimal politicsScore;
    private BigDecimal historyScore;
    private BigDecimal geographyScore;
    
    private BigDecimal totalScore;
    private Integer classRank;
    private Integer gradeRank;
    private String predictedUniversity;
    private Integer scoreChange;
    
    @CreationTimestamp
    private LocalDateTime createdTime;
    
    @UpdateTimestamp
    private LocalDateTime updatedTime;
    
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer deleted = 0;
}
