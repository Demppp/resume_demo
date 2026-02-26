package com.classmanagement.controller;

import com.classmanagement.dto.AiSearchRequest;
import com.classmanagement.dto.AiSearchResponse;
import com.classmanagement.dto.Result;
import com.classmanagement.service.AiSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiSearchController {
    
    private final AiSearchService aiSearchService;
    
    @PostMapping("/search")
    public Result<AiSearchResponse> search(@RequestBody AiSearchRequest request) {
        try {
            AiSearchResponse response = aiSearchService.search(request.getQuery());
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("搜索失败：" + e.getMessage());
        }
    }
}

