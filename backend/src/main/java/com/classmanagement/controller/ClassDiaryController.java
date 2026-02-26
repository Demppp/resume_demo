package com.classmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.ClassDiary;
import com.classmanagement.service.ClassDiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
public class ClassDiaryController {
    
    private final ClassDiaryService classDiaryService;
    
    @GetMapping("/list")
    public Result<Page<ClassDiary>> getDiaryList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<ClassDiary> page = classDiaryService.getDiaryList(pageNum, pageSize, className, startDate, endDate);
        return Result.success(page);
    }
    
    @PostMapping("/add")
    public Result<ClassDiary> addDiary(@RequestBody ClassDiary diary) {
        ClassDiary result = classDiaryService.addDiary(diary);
        return Result.success("添加日志成功，AI已生成摘要", result);
    }
    
    @PutMapping("/update")
    public Result<ClassDiary> updateDiary(@RequestBody ClassDiary diary) {
        ClassDiary result = classDiaryService.updateDiary(diary);
        return Result.success("更新日志成功", result);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id) {
        classDiaryService.deleteDiary(id);
        return Result.success("删除日志成功", null);
    }
    
    @GetMapping("/detail/{id}")
    public Result<ClassDiary> getDiaryById(@PathVariable Long id) {
        ClassDiary diary = classDiaryService.getDiaryById(id);
        return Result.success(diary);
    }
}

