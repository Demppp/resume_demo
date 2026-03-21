package com.classmanagement.repository;

import com.classmanagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    List<Attendance> findByStudentIdAndAttendanceDateAfterOrderByAttendanceDateDesc(
            Long studentId, LocalDate date);

    List<Attendance> findByClassName(String className);

    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.attendanceDate >= :startDate AND a.attendanceStatus != '正常'")
    List<Attendance> findAbnormalByStudentIdSince(@Param("studentId") Long studentId,
                                                   @Param("startDate") LocalDate startDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId " +
           "AND a.attendanceDate >= :startDate AND a.attendanceStatus != '正常'")
    long countAbnormalByStudentIdSince(@Param("studentId") Long studentId,
                                        @Param("startDate") LocalDate startDate);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceStatus != '正常' " +
           "ORDER BY a.attendanceDate DESC")
    List<Attendance> findAllAbnormal();
}

