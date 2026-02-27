package com.classmanagement.dto;

import lombok.Data;

@Data
public class ClassRankingDTO {
    private String className;
    private String classType;
    private String avgScore;
    private Integer studentCount;
}

