package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operationType;
    private String operationModule;
    private String operationDesc;
    private String operatorName;
    private String requestMethod;
    private String requestUrl;

    @Column(columnDefinition = "TEXT")
    private String requestParams;

    private String ipAddress;
    private Long executionTime;

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer deleted = 0;
}
