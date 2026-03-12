package com.classmanagement.controller;

import com.classmanagement.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/students")
    public void exportStudents(
            @RequestParam(required = false) String className,
            HttpServletResponse response) throws Exception {
        exportService.exportStudents(className, response);
    }

    @GetMapping("/scores")
    public void exportScores(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String examName,
            HttpServletResponse response) throws Exception {
        exportService.exportScores(className, examName, response);
    }

    @GetMapping("/attendance")
    public void exportAttendance(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws Exception {
        exportService.exportAttendance(className, startDate, endDate, response);
    }
}
