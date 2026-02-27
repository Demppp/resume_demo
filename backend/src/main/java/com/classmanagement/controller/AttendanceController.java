package com.classmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.Attendance;
import com.classmanagement.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    @GetMapping("/list")
    public Result<Page<Attendance>> getAttendanceList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<Attendance> page = attendanceService.getAttendanceList(pageNum, pageSize, className, studentName, startDate, endDate);
        return Result.success(page);
    }
    
    @PostMapping("/add")
    public Result<Attendance> addAttendance(@RequestBody Attendance attendance) {
        Attendance result = attendanceService.addAttendance(attendance);
        return Result.success("添加考勤记录成功", result);
    }
    
    @PutMapping("/update")
    public Result<Attendance> updateAttendance(@RequestBody Attendance attendance) {
        Attendance result = attendanceService.updateAttendance(attendance);
        return Result.success("更新考勤记录成功", result);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return Result.success("删除考勤记录成功", null);
    }
    
    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getStudentAttendance(@PathVariable Long studentId) {
        List<Attendance> list = attendanceService.getStudentAttendance(studentId);
        return Result.success(list);
    }
}

