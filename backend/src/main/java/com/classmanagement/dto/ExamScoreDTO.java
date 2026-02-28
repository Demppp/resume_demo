package com.classmanagement.dto;

import com.classmanagement.entity.ExamScore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExamScoreDTO {
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
    private Integer scoreChange;  // 与上次考试的分数变化
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    public static ExamScoreDTO fromEntity(ExamScore entity) {
        ExamScoreDTO dto = new ExamScoreDTO();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudentId());
        dto.setStudentName(entity.getStudentName());
        dto.setClassName(entity.getClassName());
        dto.setClassType(entity.getClassType());
        dto.setExamName(entity.getExamName());
        dto.setExamDate(entity.getExamDate());
        dto.setChineseScore(entity.getChineseScore());
        dto.setMathScore(entity.getMathScore());
        dto.setEnglishScore(entity.getEnglishScore());
        dto.setComprehensiveScore(entity.getComprehensiveScore());
        dto.setPhysicsScore(entity.getPhysicsScore());
        dto.setChemistryScore(entity.getChemistryScore());
        dto.setBiologyScore(entity.getBiologyScore());
        dto.setPoliticsScore(entity.getPoliticsScore());
        dto.setHistoryScore(entity.getHistoryScore());
        dto.setGeographyScore(entity.getGeographyScore());
        dto.setTotalScore(entity.getTotalScore());
        dto.setClassRank(entity.getClassRank());
        dto.setGradeRank(entity.getGradeRank());
        dto.setPredictedUniversity(entity.getPredictedUniversity());
        dto.setScoreChange(entity.getScoreChange());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setUpdatedTime(entity.getUpdatedTime());
        return dto;
    }
}

