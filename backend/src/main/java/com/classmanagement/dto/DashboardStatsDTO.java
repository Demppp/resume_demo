package com.classmanagement.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Integer studentCount;
    private Integer classCount;
    private String avgScore;
    private Integer excellentCount;
}

