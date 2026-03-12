package com.classmanagement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.dto.Result;
import com.classmanagement.entity.OperationLog;
import com.classmanagement.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operation-log")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<Page<OperationLog>> getLogList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<OperationLog> page = operationLogService.getLogList(pageNum, pageSize,
                operationType, operationModule, startDate, endDate);
        return Result.success(page);
    }
}
