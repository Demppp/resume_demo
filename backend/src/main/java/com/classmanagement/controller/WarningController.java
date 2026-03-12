package com.classmanagement.controller;

import com.classmanagement.dto.WarningDTO;
import com.classmanagement.service.WarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/warning")
@CrossOrigin
public class WarningController {

    @Autowired
    private WarningService warningService;

    @GetMapping("/list")
    public Map<String, Object> getWarningList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<WarningDTO> page = 
                warningService.getWarningPage(pageNum, pageSize);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", page);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取预警数据失败: " + e.getMessage());
        }
        return result;
    }
}

