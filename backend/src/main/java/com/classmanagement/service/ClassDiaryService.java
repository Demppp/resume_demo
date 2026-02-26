package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.entity.ClassDiary;
import com.classmanagement.mapper.ClassDiaryMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ClassDiaryService {
    
    private final ClassDiaryMapper classDiaryMapper;
    private final ChatLanguageModel chatLanguageModel;
    
    public Page<ClassDiary> getDiaryList(int pageNum, int pageSize, String className, LocalDate startDate, LocalDate endDate) {
        Page<ClassDiary> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ClassDiary> wrapper = new LambdaQueryWrapper<>();
        
        if (className != null && !className.isEmpty()) {
            wrapper.eq(ClassDiary::getClassName, className);
        }
        if (startDate != null) {
            wrapper.ge(ClassDiary::getDiaryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(ClassDiary::getDiaryDate, endDate);
        }
        
        wrapper.orderByDesc(ClassDiary::getDiaryDate);
        
        return classDiaryMapper.selectPage(page, wrapper);
    }
    
    public ClassDiary addDiary(ClassDiary diary) {
        // 使用AI生成摘要
        String summary = generateAiSummary(diary.getDiaryContent());
        diary.setAiSummary(summary);
        
        classDiaryMapper.insert(diary);
        return diary;
    }
    
    private String generateAiSummary(String content) {
        String prompt = String.format(
            "请将以下班干部日志总结成一句话，要求简洁明了，突出重点：\n%s\n只返回总结内容，不要其他文字。",
            content
        );
        
        return chatLanguageModel.generate(prompt);
    }
    
    public ClassDiary updateDiary(ClassDiary diary) {
        // 重新生成AI摘要
        String summary = generateAiSummary(diary.getDiaryContent());
        diary.setAiSummary(summary);
        
        classDiaryMapper.updateById(diary);
        return diary;
    }
    
    public void deleteDiary(Long id) {
        classDiaryMapper.deleteById(id);
    }
    
    public ClassDiary getDiaryById(Long id) {
        return classDiaryMapper.selectById(id);
    }
}

