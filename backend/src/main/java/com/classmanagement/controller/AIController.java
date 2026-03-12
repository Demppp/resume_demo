package com.classmanagement.controller;

import com.classmanagement.dto.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @PostMapping("/search")
    public Result<?> aiSearch(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        
        if (query == null || query.trim().isEmpty()) {
            return Result.error("查询内容不能为空");
        }
        
        query = query.toLowerCase();
        Map<String, Object> result = new HashMap<>();
        
        // 解析查询意图
        if (query.contains("成绩") || query.contains("分数") || query.contains("考试") || query.contains("周考")) {
            result.put("success", true);
            result.put("description", "成绩信息");
            
            // 构建路由参数
            StringBuilder route = new StringBuilder("/exam?");
            
            // 提取学生姓名
            String studentName = extractStudentName(query);
            if (studentName != null) {
                route.append("studentName=").append(studentName).append("&");
            }
            
            // 提取考试名称
            String examName = extractExamName(query);
            if (examName != null) {
                route.append("examName=").append(examName).append("&");
            }
            
            // 提取班级
            String className = extractClassName(query);
            if (className != null) {
                route.append("className=").append(className);
            }
            
            result.put("route", route.toString());
            
        } else if (query.contains("学生") || query.contains("名单") || query.contains("列表")) {
            result.put("success", true);
            result.put("description", "学生信息");
            
            StringBuilder route = new StringBuilder("/student?");
            String className = extractClassName(query);
            if (className != null) {
                route.append("className=").append(className);
            }
            
            result.put("route", route.toString());
            
        } else if (query.contains("考勤") || query.contains("出勤") || query.contains("请假") || query.contains("迟到")) {
            result.put("success", true);
            result.put("description", "考勤记录");
            
            StringBuilder route = new StringBuilder("/attendance?");
            String studentName = extractStudentName(query);
            if (studentName != null) {
                route.append("studentName=").append(studentName);
            }
            
            result.put("route", route.toString());
            
        } else if (query.contains("日志") || query.contains("班干部")) {
            result.put("success", true);
            result.put("description", "班干部日志");
            
            StringBuilder route = new StringBuilder("/diary?");
            String className = extractClassName(query);
            if (className != null) {
                route.append("className=").append(className);
            }
            
            result.put("route", route.toString());
            
        } else if (query.contains("预警") || query.contains("警告")) {
            result.put("success", true);
            result.put("description", "预警信息");
            result.put("route", "/warning");
            
        } else if (query.contains("分析") || query.contains("统计") || query.contains("数据")) {
            result.put("success", true);
            result.put("description", "数据分析");
            result.put("route", "/analytics");
            
        } else if (query.contains("看板") || query.contains("概览") || query.contains("首页")) {
            result.put("success", true);
            result.put("description", "数据看板");
            result.put("route", "/dashboard");
            
        } else {
            result.put("success", false);
            result.put("message", "抱歉，无法理解您的需求。您可以尝试：\n" +
                    "- 查看某位学生的成绩\n" +
                    "- 查看某个班级的学生列表\n" +
                    "- 查看考勤记录\n" +
                    "- 查看班干部日志");
        }
        
        return Result.success(result);
    }
    
    // 提取学生姓名
    private String extractStudentName(String query) {
        String[] commonNames = {"张伟", "王芳", "李明", "赵静", "陈强", "刘丽", "杨勇", "黄艳", "周涛", "吴平",
                "张三", "李四", "王五", "赵六", "钱七", "孙浩", "马超", "朱丽", "胡军", "郭敏"};
        
        for (String name : commonNames) {
            if (query.contains(name)) {
                return name;
            }
        }
        return null;
    }
    
    // 提取考试名称
    private String extractExamName(String query) {
        if (query.contains("第一次月考")) {
            return "第一次月考";
        } else if (query.contains("第二次月考")) {
            return "第二次月考";
        } else if (query.contains("第三周周考")) {
            return "第三周周考";
        } else if (query.contains("第一周") || query.contains("一周")) {
            return "第一周周考";
        }
        return null;
    }
    
    // 提取班级
    private String extractClassName(String query) {
        if (query.contains("高三1班") || query.contains("一班") || query.contains("1班")) {
            return "高三1班";
        } else if (query.contains("高三2班") || query.contains("二班") || query.contains("2班")) {
            return "高三2班";
        } else if (query.contains("高三3班") || query.contains("三班") || query.contains("3班")) {
            return "高三3班";
        } else if (query.contains("高三4班") || query.contains("四班") || query.contains("4班")) {
            return "高三4班";
        } else if (query.contains("高三5班") || query.contains("五班") || query.contains("5班")) {
            return "高三5班";
        } else if (query.contains("高三6班") || query.contains("六班") || query.contains("6班")) {
            return "高三6班";
        }
        return null;
    }
}

