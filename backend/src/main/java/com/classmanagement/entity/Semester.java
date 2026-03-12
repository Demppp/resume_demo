package com.classmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("semester")
public class Semester {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String semesterName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;
}
