package com.classmanagement.service;

import com.classmanagement.dto.PageResult;
import com.classmanagement.entity.Attendance;
import com.classmanagement.repository.AttendanceRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    
    private final AttendanceRepository attendanceRepository;
    private final StudentProfileEmbeddingService studentProfileEmbeddingService;
    
    public PageResult<Attendance> getAttendanceList(int pageNum, int pageSize, String className, String studentName, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "attendanceDate"));
        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (className != null && !className.isBlank()) {
                predicates.add(cb.equal(root.get("className"), className));
        }
            if (studentName != null && !studentName.isBlank()) {
                predicates.add(cb.like(root.get("studentName"), "%" + studentName + "%"));
        }
        if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate));
        }
        if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate));
        }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Attendance> page = attendanceRepository.findAll(spec, pageable);
        return PageResult.from(page);
    }
    
    public Attendance addAttendance(Attendance attendance) {
        Attendance saved = attendanceRepository.save(attendance);
        if (saved.getStudentId() != null) {
            studentProfileEmbeddingService.buildAndSaveEmbedding(saved.getStudentId());
        }
        return saved;
    }
    
    public Attendance updateAttendance(Attendance attendance) {
        Attendance saved = attendanceRepository.save(attendance);
        if (saved.getStudentId() != null) {
            studentProfileEmbeddingService.buildAndSaveEmbedding(saved.getStudentId());
        }
        return saved;
    }
    
    public void deleteAttendance(Long id) {
        Attendance attendance = attendanceRepository.findById(id).orElse(null);
        if (attendance == null) return;
        attendance.setDeleted(1);
        attendanceRepository.save(attendance);
        if (attendance.getStudentId() != null) {
            studentProfileEmbeddingService.buildAndSaveEmbedding(attendance.getStudentId());
        }
    }
    
    public List<Attendance> getStudentAttendance(Long studentId) {
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
    }
}
