package com.classmanagement.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classmanagement.entity.Attendance;
import com.classmanagement.entity.ExamScore;
import com.classmanagement.entity.Student;
import com.classmanagement.mapper.AttendanceMapper;
import com.classmanagement.mapper.ExamScoreMapper;
import com.classmanagement.mapper.StudentMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final StudentMapper studentMapper;
    private final ExamScoreMapper examScoreMapper;
    private final AttendanceMapper attendanceMapper;

    public void exportStudents(String className, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            wrapper.eq(Student::getClassName, className);
        }
        wrapper.orderByAsc(Student::getClassName);
        wrapper.orderByAsc(Student::getStudentName);
        List<Student> list = studentMapper.selectList(wrapper);

        // 构造导出数据
        List<List<String>> head = new ArrayList<>();
        head.add(List.of("序号"));
        head.add(List.of("姓名"));
        head.add(List.of("性别"));
        head.add(List.of("班级"));
        head.add(List.of("科类"));
        head.add(List.of("学号"));
        head.add(List.of("家长电话"));
        head.add(List.of("地址"));

        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (Student s : list) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(s.getStudentName());
            row.add(s.getGender());
            row.add(s.getClassName());
            row.add(s.getClassType());
            row.add(s.getStudentNumber());
            row.add(s.getParentPhone());
            row.add(s.getAddress());
            data.add(row);
        }

        String fileName = "学生花名册";
        if (className != null && !className.isEmpty()) {
            fileName = className + "_" + fileName;
        }
        setExcelResponse(response, fileName);
        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("学生花名册")
                .doWrite(data);
    }

    public void exportScores(String className, String examName, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<ExamScore> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            wrapper.eq(ExamScore::getClassName, className);
        }
        if (examName != null && !examName.isEmpty()) {
            wrapper.eq(ExamScore::getExamName, examName);
        }
        wrapper.orderByAsc(ExamScore::getGradeRank);
        List<ExamScore> list = examScoreMapper.selectList(wrapper);

        List<List<String>> head = new ArrayList<>();
        head.add(List.of("排名"));
        head.add(List.of("姓名"));
        head.add(List.of("班级"));
        head.add(List.of("科类"));
        head.add(List.of("考试名称"));
        head.add(List.of("语文"));
        head.add(List.of("数学"));
        head.add(List.of("英语"));
        head.add(List.of("文综/理综"));
        head.add(List.of("总分"));
        head.add(List.of("班级排名"));
        head.add(List.of("年级排名"));
        head.add(List.of("预测大学"));

        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (ExamScore s : list) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(s.getStudentName());
            row.add(s.getClassName());
            row.add(s.getClassType());
            row.add(s.getExamName());
            row.add(s.getChineseScore());
            row.add(s.getMathScore());
            row.add(s.getEnglishScore());
            row.add(s.getComprehensiveScore());
            row.add(s.getTotalScore());
            row.add(s.getClassRank());
            row.add(s.getGradeRank());
            row.add(s.getPredictedUniversity());
            data.add(row);
        }

        String fileName = "成绩单";
        if (examName != null && !examName.isEmpty()) fileName = examName + "_" + fileName;
        if (className != null && !className.isEmpty()) fileName = className + "_" + fileName;
        setExcelResponse(response, fileName);
        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("成绩单")
                .doWrite(data);
    }

    public void exportAttendance(String className, String startDate, String endDate,
                                   HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (className != null && !className.isEmpty()) {
            wrapper.eq(Attendance::getClassName, className);
        }
        wrapper.orderByDesc(Attendance::getAttendanceDate);
        wrapper.orderByAsc(Attendance::getClassName);
        List<Attendance> list = attendanceMapper.selectList(wrapper);

        List<List<String>> head = new ArrayList<>();
        head.add(List.of("序号"));
        head.add(List.of("姓名"));
        head.add(List.of("班级"));
        head.add(List.of("日期"));
        head.add(List.of("考勤状态"));
        head.add(List.of("原因"));

        List<List<Object>> data = new ArrayList<>();
        int index = 1;
        for (Attendance a : list) {
            List<Object> row = new ArrayList<>();
            row.add(index++);
            row.add(a.getStudentName());
            row.add(a.getClassName());
            row.add(a.getAttendanceDate() != null ? a.getAttendanceDate().toString() : "");
            row.add(a.getAttendanceStatus());
            row.add(a.getReason());
            data.add(row);
        }

        setExcelResponse(response, "考勤记录");
        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet("考勤记录")
                .doWrite(data);
    }

    private void setExcelResponse(HttpServletResponse response, String fileName) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }
}
