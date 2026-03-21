package com.classmanagement.controller;

import com.classmanagement.dto.ExamScoreDTO;
import com.classmanagement.dto.PageResult;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.service.ExamScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamScoreController {
    
    private final ExamScoreService examScoreService;
    
    @GetMapping("/list")
    public Result<PageResult<ExamScoreDTO>> getExamScoreList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String examName,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) BigDecimal minScore,
            @RequestParam(required = false) BigDecimal maxScore) {
        return Result.success(examScoreService.getExamScoreList(pageNum, pageSize, className, examName, studentName, minScore, maxScore));
    }
    
    @GetMapping("/stats")
    public Result<Map<String, Object>> getExamStats(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String examName,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) BigDecimal minScore,
            @RequestParam(required = false) BigDecimal maxScore) {
        return Result.success(examScoreService.getExamStats(className, examName, studentName, minScore, maxScore));
    }
    
    @PostMapping("/add")
    public Result<ExamScore> addExamScore(@RequestBody ExamScore examScore) {
        return Result.success("添加成绩成功", examScoreService.addExamScore(examScore));
    }
    
    @PutMapping("/update")
    public Result<ExamScore> updateExamScore(@RequestBody ExamScore examScore) {
        return Result.success("更新成绩成功", examScoreService.updateExamScore(examScore));
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteExamScore(@PathVariable Long id) {
        examScoreService.deleteExamScore(id);
        return Result.success("删除成绩成功", null);
    }
    
    @GetMapping("/student/{studentId}")
    public Result<List<ExamScore>> getStudentScores(@PathVariable Long studentId) {
        return Result.success(examScoreService.getStudentScores(studentId));
    }
}
