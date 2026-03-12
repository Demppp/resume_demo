package com.classmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String operationType;    // CREATE, UPDATE, DELETE
    private String operationModule;  // student, exam, attendance, diary
    private String operationDesc;    // 操作描述
    private String operatorName;     // 操作人（暂默认"年级主任"）
    private String requestMethod;    // GET, POST, PUT, DELETE
    private String requestUrl;       // 请求地址
    private String requestParams;    // 请求参数（JSON）
    private String ipAddress;        // IP地址
    private Long executionTime;      // 执行耗时(ms)

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
