package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student")
@SQLRestriction("deleted = 0")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String studentName;
    private String gender;
    private String address;
    private String parentPhone;
    private Long classId;
    private String className;
    private String classType;
    private String studentNumber;
    
    @CreationTimestamp
    private LocalDateTime createdTime;
    
    @UpdateTimestamp
    private LocalDateTime updatedTime;
    
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer deleted = 0;
}
