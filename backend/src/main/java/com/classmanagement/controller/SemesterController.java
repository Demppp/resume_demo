package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import com.classmanagement.entity.Semester;
import com.classmanagement.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/semester")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping("/list")
    public Result<List<Semester>> getAllSemesters() {
        return Result.success(semesterService.getAllSemesters());
    }

    @GetMapping("/current")
    public Result<Semester> getCurrentSemester() {
        return Result.success(semesterService.getCurrentSemester());
    }

    @PostMapping("/add")
    public Result<Semester> addSemester(@RequestBody Semester semester) {
        return Result.success("添加学期成功", semesterService.addSemester(semester));
    }

    @PutMapping("/update")
    public Result<Semester> updateSemester(@RequestBody Semester semester) {
        return Result.success("更新学期成功", semesterService.updateSemester(semester));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return Result.success("删除学期成功", null);
    }

    @PutMapping("/set-current/{id}")
    public Result<Void> setCurrentSemester(@PathVariable Long id) {
        semesterService.setCurrentSemester(id);
        return Result.success("设置当前学期成功", null);
    }
}
