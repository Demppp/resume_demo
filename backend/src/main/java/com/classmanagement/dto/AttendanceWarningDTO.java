package com.classmanagement.dto;

import lombok.Data;

@Data
public class AttendanceWarningDTO {
    private String studentName;
    private String className;
    private Integer abnormalCount;
    private String latestStatus;
    private Long studentId;
}

