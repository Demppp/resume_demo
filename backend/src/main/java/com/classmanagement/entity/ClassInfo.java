package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "class_info")
@SQLRestriction("deleted = 0")
public class ClassInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String className;
    private String classType;
    private String teacherName;
    private Integer studentCount;
    
    @CreationTimestamp
    private LocalDateTime createdTime;
    
    @UpdateTimestamp
    private LocalDateTime updatedTime;
    
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer deleted = 0;
}
