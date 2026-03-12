package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import com.classmanagement.entity.Notification;
import com.classmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notification>> getNotificationList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(notificationService.getNotificationPage(pageNum, pageSize));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        return Result.success(Map.of("count", notificationService.getUnreadCount()));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("已标记为已读", null);
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success("全部标记为已读", null);
    }
}
