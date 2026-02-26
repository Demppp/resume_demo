package com.classmanagement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("class_diary")
public class ClassDiary {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String className;
    private LocalDate diaryDate;
    private String recorderName;
    private String diaryContent;
    private String aiSummary;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
    
    @TableLogic
    private Integer deleted;
}

