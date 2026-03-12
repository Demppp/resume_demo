package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.entity.OperationLog;
import com.classmanagement.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public void save(OperationLog log) {
        operationLogMapper.insert(log);
    }

    public Page<OperationLog> getLogList(int pageNum, int pageSize,
                                          String operationType, String operationModule,
                                          String startDate, String endDate) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (operationType != null && !operationType.isEmpty()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        if (operationModule != null && !operationModule.isEmpty()) {
            wrapper.eq(OperationLog::getOperationModule, operationModule);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLog::getCreatedTime, LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLog::getCreatedTime, LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX));
        }
        wrapper.orderByDesc(OperationLog::getCreatedTime);
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}
