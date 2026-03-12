package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import com.classmanagement.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student-profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping("/{studentId}")
    public Result<Map<String, Object>> getStudentProfile(@PathVariable Long studentId) {
        Map<String, Object> profile = studentProfileService.getStudentProfile(studentId);
        return Result.success(profile);
    }
}
