package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import com.classmanagement.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
