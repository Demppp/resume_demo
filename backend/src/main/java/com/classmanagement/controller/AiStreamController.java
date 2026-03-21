package com.classmanagement.controller;

import com.classmanagement.service.AiChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AiStreamController {

    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final AiChatService aiChatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * SSE 流式对话接口 - 打字机效果
     * GET /api/ai/chat/stream?message=xxx&history=json
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
