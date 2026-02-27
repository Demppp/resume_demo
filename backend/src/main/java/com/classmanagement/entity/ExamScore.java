package com.classmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("exam_score")
public class ExamScore {
    @TableId(type = IdType.AUTO)
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
    
    // 理科：物理、化学、生物
    private BigDecimal physicsScore;
    private BigDecimal chemistryScore;
    private BigDecimal biologyScore;
    
    // 文科：政治、历史、地理
    private BigDecimal politicsScore;
    private BigDecimal historyScore;
    private BigDecimal geographyScore;
    
    private BigDecimal totalScore;
    private Integer classRank;
    private Integer gradeRank;
    private String predictedUniversity;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
}

