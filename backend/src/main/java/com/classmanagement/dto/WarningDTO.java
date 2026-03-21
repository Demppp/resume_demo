package com.classmanagement.dto;

import lombok.Data;

@Data
public class WarningDTO {
    private Long studentId;
    private String studentName;
    private String className;
    private String warningType;
    private String warningContent;
    private String warningLevel;
    private String warningTime;
    private String warningMessage;
}