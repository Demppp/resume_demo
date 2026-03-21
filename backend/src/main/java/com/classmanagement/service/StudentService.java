package com.classmanagement.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.classmanagement.dto.PageResult;
import com.classmanagement.entity.ClassInfo;
import com.classmanagement.entity.Student;
import com.classmanagement.repository.ClassInfoRepository;
import com.classmanagement.repository.StudentRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final ClassInfoRepository classInfoRepository;
    private final ChatLanguageModel chatLanguageModel;
    private final StudentProfileEmbeddingService studentProfileEmbeddingService;
    
    public PageResult<Student> getStudentList(int pageNum, int pageSize, String className, String classType) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (className != null && !className.isBlank()) {
                predicates.add(cb.equal(root.get("className"), className));
        }
            if (classType != null && !classType.isBlank()) {
                predicates.add(cb.equal(root.get("classType"), classType));
        }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Student> page = studentRepository.findAll(spec, pageable);
        return PageResult.from(page);
    }
    
    @Transactional
    public Student addStudent(Student student) {
        classInfoRepository.findByClassName(student.getClassName()).ifPresent(classInfo -> {
            student.setClassId(classInfo.getId());
            if (student.getClassType() == null || student.getClassType().isBlank()) {
            student.setClassType(classInfo.getClassType());
            }
            classInfo.setStudentCount((classInfo.getStudentCount() == null ? 0 : classInfo.getStudentCount()) + 1);
            classInfoRepository.save(classInfo);
        });

        Student saved = studentRepository.save(student);
        studentProfileEmbeddingService.buildAndSaveEmbedding(saved.getId());
        return saved;
    }
    
    @Transactional
    public Student addStudentByAi(String description) {
        String prompt = String.format(
            "请从以下描述中提取学生信息，并以JSON格式返回。\n\n" +
            "要求：\n" +
                        "1. 提取字段：studentName, gender, address, parentPhone, className, classType\n" +
                        "2. 如字段缺失，请结合上下文合理推断\n" +
                        "3. 只返回JSON，不要其他文字\n\n" +
            "描述：%s\n\n" +
                        "示例：{\"studentName\":\"张三\",\"gender\":\"男\",\"address\":\"广州市天河区\",\"parentPhone\":\"13800138000\",\"className\":\"高三1班\",\"classType\":\"理科\"}",
            description
        );
        String response = chatLanguageModel.generate(prompt);
        Student student = parseStudentFromJson(response);
        return addStudent(student);
    }
    
    private Student parseStudentFromJson(String json) {
        Student student = new Student();
        json = json.trim()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("```$", "")
                .trim();
        try {
            JSONObject jsonObject = JSON.parseObject(json);
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
    
    @Transactional
    public Student updateStudent(Student student) {
        Student saved = studentRepository.save(student);
        studentProfileEmbeddingService.buildAndSaveEmbedding(saved.getId());
        return saved;
    }
    
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) return;
        student.setDeleted(1);
        studentRepository.save(student);
        classInfoRepository.findByClassName(student.getClassName()).ifPresent(classInfo -> {
            Integer count = classInfo.getStudentCount();
            if (count != null && count > 0) {
                classInfo.setStudentCount(count - 1);
                classInfoRepository.save(classInfo);
            }
        });
    }
    
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }
    
    public List<ClassInfo> getAllClasses() {
        return classInfoRepository.findAll();
    }
}
