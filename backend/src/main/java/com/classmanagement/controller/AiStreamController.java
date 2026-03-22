package com.classmanagement.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.classmanagement.config.PromptTemplates;
import com.classmanagement.dto.Result;
import com.classmanagement.service.AiChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiStreamController {

    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final ChatLanguageModel chatLanguageModel;
    private final AiChatService aiChatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 意图识别接口 - 判断是否需要路由跳转
     * POST /api/ai/search
     */
    @PostMapping("/search")
    public Result<Map<String, Object>> search(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return Result.success(Map.of("success", false, "message", "查询内容不能为空"));
        }

        try {
            String prompt = String.format(PromptTemplates.INTENT_RECOGNITION, query);
            String response = chatLanguageModel.generate(prompt);
            
            // 尝试解析 JSON 响应
            String cleanJson = response.trim()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)^```\\s*", "")
                    .replaceAll("```$", "")
                    .trim();
            
            try {
                JSONObject intentJson = JSON.parseObject(cleanJson);
                String action = intentJson.getString("action");
                
                // 如果识别到具体意图，返回路由信息
                if (action != null && !"unknown".equals(action)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("action", action);
                    result.put("route", intentJson.getString("route"));
                    result.put("description", intentJson.getString("description"));
                    result.put("params", intentJson.getJSONObject("params"));
                    log.info("[Intent] 识别成功: action={}, route={}", action, intentJson.getString("route"));
                    return Result.success(result);
                }
            } catch (Exception e) {
                log.warn("[Intent] JSON 解析失败，返回 unknown", e);
            }
            
            // 意图未识别，返回 unknown，前端降级为流式对话
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("action", "unknown");
            result.put("message", "未识别到具体意图，将使用流式对话");
            log.info("[Intent] 意图未识别: {}", query);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("[Intent] 意图识别失败", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "意图识别失败: " + e.getMessage());
            return Result.success(result);
        }
    }

    /**
     * SSE 流式对话接口 - 打字机效果
     * GET /api/ai/chat/stream?message=xxx&history=json
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "") String history) {

        SseEmitter emitter = new SseEmitter(30000L);

        emitter.onTimeout(() -> {
            log.warn("[SSE] 连接超时，message={}", message.length() > 50 ? message.substring(0, 50) + "..." : message);
            try {
                emitter.send(SseEmitter.event().name("error").data("[TIMEOUT]"));
            } catch (Exception ignored) {}
            emitter.complete();
        });

        emitter.onError(t -> log.warn("[SSE] 连接异常断开", t));

        executor.submit(() -> {
            try {
                String prompt = aiChatService.buildStreamPrompt(message, history);

                streamingChatLanguageModel.generate(prompt, new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (IOException e) {
                            log.warn("SSE send error", e);
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        try {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.warn("SSE complete error", e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("SSE stream error", error);
                        try {
                            emitter.send(SseEmitter.event().name("error").data("[ERROR]"));
                        } catch (IOException e) {
                            log.warn("SSE error send failed", e);
                        }
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                log.error("Stream chat setup failed", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
