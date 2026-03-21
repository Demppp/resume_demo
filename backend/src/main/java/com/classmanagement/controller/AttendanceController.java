package com.classmanagement.controller;

import com.classmanagement.dto.PageResult;
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
    public Result<PageResult<Attendance>> getAttendanceList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(attendanceService.getAttendanceList(pageNum, pageSize, className, studentName, startDate, endDate));
    }
    
    @PostMapping("/add")
    public Result<Attendance> addAttendance(@RequestBody Attendance attendance) {
        return Result.success("添加考勤记录成功", attendanceService.addAttendance(attendance));
    }
    
    @PutMapping("/update")
    public Result<Attendance> updateAttendance(@RequestBody Attendance attendance) {
        return Result.success("更新考勤记录成功", attendanceService.updateAttendance(attendance));
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return Result.success("删除考勤记录成功", null);
    }
    
    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getStudentAttendance(@PathVariable Long studentId) {
        return Result.success(attendanceService.getStudentAttendance(studentId));
    }
}
