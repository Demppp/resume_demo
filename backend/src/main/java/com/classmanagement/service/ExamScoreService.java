package com.classmanagement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.mapper.ExamScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamScoreService {
    
    private final ExamScoreMapper examScoreMapper;
    
    public Page<ExamScore> getExamScoreList(int pageNum, int pageSize, String className, String examName) {
        Page<ExamScore> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        
        if (className != null && !className.isEmpty()) {
            wrapper.eq(ExamScore::getClassName, className);
        }
        if (examName != null && !examName.isEmpty()) {
            wrapper.eq(ExamScore::getExamName, examName);
        }
        
        wrapper.orderByDesc(ExamScore::getExamDate);
        
        return examScoreMapper.selectPage(page, wrapper);
    }
    
    public ExamScore addExamScore(ExamScore examScore) {
        // 计算总分
        BigDecimal total = examScore.getChineseScore()
                .add(examScore.getMathScore())
                .add(examScore.getEnglishScore())
                .add(examScore.getComprehensiveScore());
        examScore.setTotalScore(total);
        
        // 预测大学
        String predictedUniversity = predictUniversity(total, examScore.getClassType());
        examScore.setPredictedUniversity(predictedUniversity);
        
        examScoreMapper.insert(examScore);
        
        // 计算排名
        calculateRanks(examScore);
        
        return examScore;
    }
    
    private void calculateRanks(ExamScore examScore) {
        // 计算班级排名
        LambdaQueryWrapper<ExamScore> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(ExamScore::getClassName, examScore.getClassName())
                   .eq(ExamScore::getExamName, examScore.getExamName())
                   .gt(ExamScore::getTotalScore, examScore.getTotalScore());
        long classRank = examScoreMapper.selectCount(classWrapper) + 1;
        examScore.setClassRank((int) classRank);
        
        // 计算年级排名
        LambdaQueryWrapper<ExamScore> gradeWrapper = new LambdaQueryWrapper<>();
        gradeWrapper.eq(ExamScore::getExamName, examScore.getExamName())
                   .eq(ExamScore::getClassType, examScore.getClassType())
                   .gt(ExamScore::getTotalScore, examScore.getTotalScore());
        long gradeRank = examScoreMapper.selectCount(gradeWrapper) + 1;
        examScore.setGradeRank((int) gradeRank);
        
        examScoreMapper.updateById(examScore);
    }
    
    private String predictUniversity(BigDecimal totalScore, String classType) {
        // 基于2013年广东高考分数线预测
        // 理科：一本574分，二本516分
        // 文科：一本594分，二本546分
        
        double score = totalScore.doubleValue();
        
        if ("理科".equals(classType)) {
            if (score >= 650) {
                return "985高校(如中山大学、华南理工大学等)";
            } else if (score >= 600) {
                return "211高校(如暨南大学、华南师范大学等)";
            } else if (score >= 574) {
                return "一本院校";
            } else if (score >= 516) {
                return "二本院校";
            } else if (score >= 400) {
                return "专科院校";
            } else {
                return "需要加强学习";
            }
        } else {
            if (score >= 640) {
                return "985高校(如中山大学、华南理工大学等)";
            } else if (score >= 590) {
                return "211高校(如暨南大学、华南师范大学等)";
            } else if (score >= 594) {
                return "一本院校";
            } else if (score >= 546) {
                return "二本院校";
            } else if (score >= 430) {
                return "专科院校";
            } else {
                return "需要加强学习";
            }
        }
    }
    
    public ExamScore updateExamScore(ExamScore examScore) {
        // 重新计算总分
        BigDecimal total = examScore.getChineseScore()
                .add(examScore.getMathScore())
                .add(examScore.getEnglishScore())
                .add(examScore.getComprehensiveScore());
        examScore.setTotalScore(total);
        
        // 重新预测大学
        String predictedUniversity = predictUniversity(total, examScore.getClassType());
        examScore.setPredictedUniversity(predictedUniversity);
        
        examScoreMapper.updateById(examScore);
        
        // 重新计算排名
        calculateRanks(examScore);
        
        return examScore;
    }
    
    public void deleteExamScore(Long id) {
        examScoreMapper.deleteById(id);
    }
    
    public List<ExamScore> getStudentScores(Long studentId) {
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamScore::getStudentId, studentId)
               .orderByDesc(ExamScore::getExamDate);
        return examScoreMapper.selectList(wrapper);
    }
}

