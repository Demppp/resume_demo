package com.classmanagement.controller;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知接口（面试展示版：返回空数据，避免前端报错）
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @GetMapping("/list")
    public Map<String, Object> list() {
        return ok(List.of());
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", 0);
        return ok(data);
    }

    @PostMapping("/read/{id}")
    public Map<String, Object> markAsRead(@PathVariable Long id) {
        return ok(null);
    }

    @PostMapping("/read-all")
    public Map<String, Object> markAllAsRead() {
        return ok(null);
    }

    private Map<String, Object> ok(Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("code", 200);
        res.put("message", "操作成功");
        res.put("data", data);
        return res;
    }
}

