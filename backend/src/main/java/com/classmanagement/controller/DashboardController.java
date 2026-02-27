package com.classmanagement.controller;

import com.classmanagement.dto.*;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return Result.success(stats);
    }
    
    @GetMapping("/class-rankings")
    public Result<List<ClassRankingDTO>> getClassRankings() {
        List<ClassRankingDTO> rankings = dashboardService.getClassRankings();
        return Result.success(rankings);
    }
    
    @GetMapping("/top-students")
    public Result<List<ExamScore>> getTopStudents() {
        List<ExamScore> students = dashboardService.getTopStudents();
        return Result.success(students);
    }
    
    @GetMapping("/subject-rankings")
    public Result<List<SubjectRankingDTO>> getSubjectRankings(@RequestParam String subject) {
        List<SubjectRankingDTO> rankings = dashboardService.getSubjectRankings(subject);
        return Result.success(rankings);
    }
    
    @GetMapping("/attendance-warnings")
    public Result<List<AttendanceWarningDTO>> getAttendanceWarnings() {
        List<AttendanceWarningDTO> warnings = dashboardService.getAttendanceWarnings();
        return Result.success(warnings);
    }
}

