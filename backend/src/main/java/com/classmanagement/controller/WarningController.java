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
    public Map<String, Object> getWarningList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<WarningDTO> warnings = warningService.getWarningList();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", warnings);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取预警数据失败: " + e.getMessage());
        }
        return result;
    }
}

