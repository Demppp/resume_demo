-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `operation_type` VARCHAR(20) COMMENT '操作类型(CREATE/UPDATE/DELETE)',
    `operation_module` VARCHAR(50) COMMENT '操作模块',
    `operation_desc` VARCHAR(200) COMMENT '操作描述',
    `operator_name` VARCHAR(50) DEFAULT '年级主任' COMMENT '操作人',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_url` VARCHAR(200) COMMENT '请求地址',
    `request_params` TEXT COMMENT '请求参数',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `execution_time` BIGINT COMMENT '执行耗时(ms)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT COMMENT '关联学生ID',
    `student_name` VARCHAR(50) COMMENT '学生姓名',
    `class_name` VARCHAR(50) COMMENT '班级',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `type` VARCHAR(20) DEFAULT 'info' COMMENT '通知类型(score/attendance/system/warning)',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读(0未读/1已读)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 学期表
CREATE TABLE IF NOT EXISTS `semester` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `semester_name` VARCHAR(100) NOT NULL COMMENT '学期名称',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `is_current` TINYINT DEFAULT 0 COMMENT '是否当前学期(0否/1是)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期表';

-- 插入默认学期
INSERT INTO `semester` (`semester_name`, `start_date`, `end_date`, `is_current`) VALUES
('2013年春季学期', '2013-02-25', '2013-07-05', 1);
