package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import com.classmanagement.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/send")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
        Map<String, Object> result = aiChatService.chat(message, history);
        return Result.success(result);
    }

    /**
     * 生成学生学情分析报告
     * RAG思路：检索学生多维度数据 -> 增强Prompt上下文 -> LLM生成结构化报告
     */
    @PostMapping("/student-report")
    public Result<Map<String, Object>> generateStudentReport(@RequestBody Map<String, Object> request) {
        Long studentId = Long.valueOf(request.get("studentId").toString());
        Map<String, Object> result = aiChatService.generateStudentReport(studentId);
        return Result.success(result);
    }
}