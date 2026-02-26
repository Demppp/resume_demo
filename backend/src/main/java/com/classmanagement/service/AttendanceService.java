package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.entity.Attendance;
import com.classmanagement.mapper.AttendanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    
    private final AttendanceMapper attendanceMapper;
    
    public Page<Attendance> getAttendanceList(int pageNum, int pageSize, String className, LocalDate startDate, LocalDate endDate) {
        Page<Attendance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        
        if (className != null && !className.isEmpty()) {
            wrapper.eq(Attendance::getClassName, className);
        }
        if (startDate != null) {
            wrapper.ge(Attendance::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Attendance::getAttendanceDate, endDate);
        }
        
        wrapper.orderByDesc(Attendance::getAttendanceDate);
        
        return attendanceMapper.selectPage(page, wrapper);
    }
    
    public Attendance addAttendance(Attendance attendance) {
        attendanceMapper.insert(attendance);
        return attendance;
    }
    
    public Attendance updateAttendance(Attendance attendance) {
        attendanceMapper.updateById(attendance);
        return attendance;
    }
    
    public void deleteAttendance(Long id) {
        attendanceMapper.deleteById(id);
    }
    
    public List<Attendance> getStudentAttendance(Long studentId) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getStudentId, studentId)
               .orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectList(wrapper);
    }
}

