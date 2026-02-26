package com.classmanagement.dto;

import lombok.Data;

@Data
public class StudentDTO {
    private Long id;
    private String studentName;
    private String gender;
    private String address;
    private String parentPhone;
    private Long classId;
    private String className;
    private String classType;
    private String studentNumber;
}

