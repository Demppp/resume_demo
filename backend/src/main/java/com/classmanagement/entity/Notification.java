package com.classmanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notification")
@SQLRestriction("deleted = 0")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;
    private String className;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String type;
    private Boolean isRead;

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer deleted = 0;
}
