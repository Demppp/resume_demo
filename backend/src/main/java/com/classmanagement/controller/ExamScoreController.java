package com.classmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.ExamScoreDTO;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.service.ExamScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamScoreController {
    
    private final ExamScoreService examScoreService;
    
    @GetMapping("/list")
    public Result<Page<ExamScoreDTO>> getExamScoreList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String examName,
            @RequestParam(required = false) String studentName) {
        Page<ExamScoreDTO> page = examScoreService.getExamScoreList(pageNum, pageSize, className, examName, studentName);
        return Result.success(page);
    }
    
    @PostMapping("/add")
    public Result<ExamScore> addExamScore(@RequestBody ExamScore examScore) {
        ExamScore result = examScoreService.addExamScore(examScore);
        return Result.success("添加成绩成功", result);
    }
    
    @PutMapping("/update")
    public Result<ExamScore> updateExamScore(@RequestBody ExamScore examScore) {
        ExamScore result = examScoreService.updateExamScore(examScore);
        return Result.success("更新成绩成功", result);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteExamScore(@PathVariable Long id) {
        examScoreService.deleteExamScore(id);
        return Result.success("删除成绩成功", null);
    }
    
    @GetMapping("/student/{studentId}")
    public Result<List<ExamScore>> getStudentScores(@PathVariable Long studentId) {
        List<ExamScore> list = examScoreService.getStudentScores(studentId);
        return Result.success(list);
    }
}

