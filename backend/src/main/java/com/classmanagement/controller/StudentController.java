package com.classmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.AiStudentRequest;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.ClassInfo;
import com.classmanagement.entity.Student;
import com.classmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    
    @GetMapping("/list")
    public Result<Page<Student>> getStudentList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String classType) {
        Page<Student> page = studentService.getStudentList(pageNum, pageSize, className, classType);
        return Result.success(page);
    }
    
    @PostMapping("/add")
    public Result<Student> addStudent(@RequestBody Student student) {
        Student result = studentService.addStudent(student);
        return Result.success("添加学生成功", result);
    }
    
    @PostMapping("/add-by-ai")
    public Result<Student> addStudentByAi(@RequestBody AiStudentRequest request) {
        try {
            Student result = studentService.addStudentByAi(request.getDescription());
            return Result.success("AI添加学生成功", result);
        } catch (Exception e) {
            return Result.error("AI解析失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/update")
    public Result<Student> updateStudent(@RequestBody Student student) {
        Student result = studentService.updateStudent(student);
        return Result.success("更新学生信息成功", result);
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success("删除学生成功", null);
    }
    
    @GetMapping("/detail/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return Result.success(student);
    }
    
    @GetMapping("/classes")
    public Result<List<ClassInfo>> getAllClasses() {
        List<ClassInfo> classes = studentService.getAllClasses();
        return Result.success(classes);
    }
}

