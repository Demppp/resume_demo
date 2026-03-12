package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanagement.entity.Semester;
import com.classmanagement.mapper.SemesterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterMapper semesterMapper;

    public List<Semester> getAllSemesters() {
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Semester::getIsCurrent);
        wrapper.orderByDesc(Semester::getStartDate);
        return semesterMapper.selectList(wrapper);
    }

    public Semester getCurrentSemester() {
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Semester::getIsCurrent, true);
        return semesterMapper.selectOne(wrapper);
    }

    public Semester addSemester(Semester semester) {
        semesterMapper.insert(semester);
        return semester;
    }

    public Semester updateSemester(Semester semester) {
        semesterMapper.updateById(semester);
        return semester;
    }

    public void deleteSemester(Long id) {
        semesterMapper.deleteById(id);
    }

    public void setCurrentSemester(Long id) {
        // 先将所有学期设为非当前
        List<Semester> all = semesterMapper.selectList(null);
        for (Semester s : all) {
            s.setIsCurrent(false);
            semesterMapper.updateById(s);
        }
        // 设为当前学期
        Semester semester = semesterMapper.selectById(id);
        if (semester != null) {
            semester.setIsCurrent(true);
            semesterMapper.updateById(semester);
        }
    }
}
