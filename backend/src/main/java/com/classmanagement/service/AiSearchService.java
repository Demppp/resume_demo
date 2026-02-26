package com.classmanagement.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanagement.dto.AiSearchResponse;
import com.classmanagement.entity.Student;
import com.classmanagement.mapper.StudentMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiSearchService {
    
    private final ChatLanguageModel chatLanguageModel;
    private final StudentMapper studentMapper;
    
    public AiSearchResponse search(String query) {
        try {
            // 使用AI解析用户意图
            String prompt = String.format(
                "请分析以下用户查询意图，并返回JSON格式的结果。\n\n" +
                "用户查询：%s\n\n" +
                "请识别：\n" +
                "1. action: 用户想做什么 (可选值: 'view_student_scores', 'view_students', 'view_attendance', 'view_diary')\n" +
                "2. studentName: 学生姓名（如果提到）\n" +
                "3. className: 班级名称（如果提到，格式如'一班'、'二班'）\n" +
                "4. examName: 考试名称（如果提到，如'第一周周考'、'第二周周考'）\n" +
                "5. description: 用一句话描述用户想查看的内容\n\n" +
                "只返回JSON，不要其他文字。\n" +
                "示例：{\"action\":\"view_student_scores\",\"studentName\":\"张三\",\"examName\":\"第一周周考\",\"description\":\"张三的第一周周考成绩\"}",
                query
            );
            
            String response = chatLanguageModel.generate(prompt);
            
            // 解析AI返回的JSON
            String cleanJson = response.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            JSONObject intent = JSON.parseObject(cleanJson);
            String action = intent.getString("action");
            String studentName = intent.getString("studentName");
            String className = intent.getString("className");
            String examName = intent.getString("examName");
            String description = intent.getString("description");
            
            // 根据意图生成路由
            String route = generateRoute(action, studentName, className, examName);
            
            if (route != null) {
                return AiSearchResponse.success(route, description);
            } else {
                return AiSearchResponse.error("抱歉，无法理解您的需求");
            }
            
        } catch (Exception e) {
            return AiSearchResponse.error("AI解析失败：" + e.getMessage());
        }
    }
    
    private String generateRoute(String action, String studentName, String className, String examName) {
        switch (action) {
            case "view_student_scores":
                // 查看学生成绩
                if (studentName != null) {
                    // 查找学生ID
                    LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Student::getStudentName, studentName);
                    Student student = studentMapper.selectOne(wrapper);
                    
                    if (student != null) {
                        // 跳转到成绩管理页面，并筛选该学生
                        String route = "/exam?studentName=" + studentName;
                        if (examName != null) {
                            route += "&examName=" + examName;
                        }
                        return route;
                    }
                }
                return "/exam" + (examName != null ? "?examName=" + examName : "");
                
            case "view_students":
                // 查看学生列表
                if (className != null) {
                    return "/student?className=" + className;
                }
                return "/student";
                
            case "view_attendance":
                // 查看考勤
                if (className != null) {
                    return "/attendance?className=" + className;
                }
                return "/attendance";
                
            case "view_diary":
                // 查看日志
                if (className != null) {
                    return "/diary?className=" + className;
                }
                return "/diary";
                
            default:
                return null;
        }
    }
}

