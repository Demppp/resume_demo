package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import com.classmanagement.service.StudentProfileEmbeddingService;
import com.classmanagement.service.StudentRagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai/rag")
@RequiredArgsConstructor
public class RagController {

    private final StudentRagService studentRagService;
    private final StudentProfileEmbeddingService studentProfileEmbeddingService;

    /**
     * 自然语言 RAG 查询
     * POST /api/ai/rag/query
     * body: {"query": "找出近三次考试退步明显且缺勤超过5次的学生"}
     */
    @PostMapping("/query")
    public Result<Map<String, Object>> query(@RequestBody Map<String, Object> request) {
        String query = request.getOrDefault("query", "").toString();
        int topK = request.containsKey("topK")
                ? Integer.parseInt(request.get("topK").toString())
                : 5;
        topK = Math.max(1, Math.min(topK, 20)); // 限制范围 1~20
        return Result.success(studentRagService.ragQuery(query, topK));
    }

    /**
     * 手动重建全部学生画像向量索引
     */
    @PostMapping("/rebuild-index")
    public Result<Map<String, Object>> rebuildIndex() {
        studentProfileEmbeddingService.rebuildAllEmbeddings();
        return Result.success(Map.of(
                "success", true,
                "message", "向量索引重建任务已提交，请稍后查询"
        ));
    }

    /**
     * 重建单个学生画像向量
     */
    @PostMapping("/rebuild/{studentId}")
    public Result<Map<String, Object>> rebuildOne(@PathVariable Long studentId) {
        studentProfileEmbeddingService.buildAndSaveEmbedding(studentId);
        return Result.success(Map.of(
                "success", true,
                "studentId", studentId,
                "message", "学生画像向量重建任务已提交"
        ));
    }
}

