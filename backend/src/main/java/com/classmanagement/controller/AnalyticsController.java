package com.classmanagement.controller;

import com.classmanagement.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@CrossOrigin
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/data")
    public Map<String, Object> getAnalyticsData(@RequestParam String examName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getAnalyticsData(examName);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取分析数据失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/ranking-trend")
    public Map<String, Object> getRankingTrend(@RequestParam String studentName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getRankingTrend(studentName);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取排名趋势失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/risk-students")
    public Map<String, Object> getRiskStudents(@RequestParam String examName) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> data = analyticsService.getRiskStudents(examName);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取风险学生失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/exams")
    public Map<String, Object> getExamList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> exams = analyticsService.getExamList();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", exams);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取考试列表失败: " + e.getMessage());
        }
        return result;
    }
}

