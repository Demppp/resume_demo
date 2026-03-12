package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.entity.Notification;
import com.classmanagement.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    
    public Page<Notification> getNotificationPage(int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notification::getCreatedTime);
        return notificationMapper.selectPage(page, wrapper);
    }

    public List<Notification> getNotificationList() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notification::getCreatedTime);
        wrapper.last("LIMIT 50");
        return notificationMapper.selectList(wrapper);
    }

    public void markAsRead(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification != null) {
            notification.setIsRead(true);
            notificationMapper.updateById(notification);
        }
    }

    public void markAllAsRead() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, false);
        List<Notification> unreadList = notificationMapper.selectList(wrapper);
        for (Notification n : unreadList) {
            n.setIsRead(true);
            notificationMapper.updateById(n);
        }
    }

    public void createNotification(Long studentId, String studentName, String className,
                                     String title, String content, String type) {
        Notification notification = new Notification();
        notification.setStudentId(studentId);
        notification.setStudentName(studentName);
        notification.setClassName(className);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);
        notificationMapper.insert(notification);
    }

    public long getUnreadCount() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getIsRead, false);
        return notificationMapper.selectCount(wrapper);
    }
}
