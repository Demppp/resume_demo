package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.entity.ClassInfo;
import com.classmanagement.entity.Student;
import com.classmanagement.mapper.ClassInfoMapper;
import com.classmanagement.mapper.StudentMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final ChatLanguageModel chatLanguageModel;
    
    public Page<Student> getStudentList(int pageNum, int pageSize, String className, String classType) {
        Page<Student> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        if (className != null && !className.isEmpty()) {
            wrapper.eq(Student::getClassName, className);
        }
        if (classType != null && !classType.isEmpty()) {
            wrapper.eq(Student::getClassType, classType);
        }
        
        return studentMapper.selectPage(page, wrapper);
    }
    
    @Transactional
    public Student addStudent(Student student) {
        // 查询班级信息
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassInfo::getClassName, student.getClassName());
        ClassInfo classInfo = classInfoMapper.selectOne(wrapper);
        
        if (classInfo != null) {
            student.setClassId(classInfo.getId());
            student.setClassType(classInfo.getClassType());
            
            // 保存学生
            studentMapper.insert(student);
            
            // 更新班级人数
            classInfo.setStudentCount(classInfo.getStudentCount() + 1);
            classInfoMapper.updateById(classInfo);
        }
        
        return student;
    }
    
    @Transactional
    public Student addStudentByAi(String description) {
        // 使用LangChain解析学生信息
        String prompt = String.format(
            "请从以下描述中提取学生信息，并以JSON格式返回。\n\n" +
            "要求：\n" +
            "1. 提取字段：\n" +
            "   - studentName(姓名)\n" +
            "   - gender(性别，根据姓名或描述推断，默认'男')\n" +
            "   - address(地址，如果只提到区，补充为'广州市XX区')\n" +
            "   - parentPhone(家长电话，如果没有提供，生成一个13800138XXX格式)\n" +
            "   - className(班级，如'一班'、'二班'等，如果说'分配四班'就是'四班')\n" +
            "   - classType(科类，'文科'或'理科')\n" +
            "2. 如果某个字段没有明确提到，请根据上下文智能推断\n" +
            "3. 年龄信息不需要提取\n" +
            "4. 只返回JSON，不要其他文字\n\n" +
            "描述：%s\n\n" +
            "JSON格式示例：\n" +
            "{\"studentName\":\"张三\",\"gender\":\"男\",\"address\":\"广州市天河区\",\"parentPhone\":\"13800138000\",\"className\":\"一班\",\"classType\":\"理科\"}",
            description
        );
        
        String response = chatLanguageModel.generate(prompt);
        
        // 解析JSON
        Student student = parseStudentFromJson(response);
        
        // 保存学生
        return addStudent(student);
    }
    
    private Student parseStudentFromJson(String json) {
        // 简单的JSON解析
        Student student = new Student();
        json = json.trim();
        
        if (json.startsWith("```json")) {
            json = json.substring(7);
        }
        if (json.startsWith("```")) {
            json = json.substring(3);
        }
        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3);
        }
        json = json.trim();
        
        try {
            com.alibaba.fastjson2.JSONObject jsonObject = com.alibaba.fastjson2.JSON.parseObject(json);
            student.setStudentName(jsonObject.getString("studentName"));
            student.setGender(jsonObject.getString("gender"));
            student.setAddress(jsonObject.getString("address"));
            student.setParentPhone(jsonObject.getString("parentPhone"));
            student.setClassName(jsonObject.getString("className"));
            student.setClassType(jsonObject.getString("classType"));
        } catch (Exception e) {
            throw new RuntimeException("AI解析失败，请检查输入格式");
        }
        
        return student;
    }
    
    public Student updateStudent(Student student) {
        studentMapper.updateById(student);
        return student;
    }
    
    public void deleteStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student != null) {
            studentMapper.deleteById(id);
            
            // 更新班级人数
            LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ClassInfo::getClassName, student.getClassName());
            ClassInfo classInfo = classInfoMapper.selectOne(wrapper);
            if (classInfo != null && classInfo.getStudentCount() > 0) {
                classInfo.setStudentCount(classInfo.getStudentCount() - 1);
                classInfoMapper.updateById(classInfo);
            }
        }
    }
    
    public Student getStudentById(Long id) {
        return studentMapper.selectById(id);
    }
    
    public List<ClassInfo> getAllClasses() {
        return classInfoMapper.selectList(null);
    }
}

