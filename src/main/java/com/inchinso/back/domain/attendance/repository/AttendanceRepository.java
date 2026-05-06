package com.inchinso.back.domain.attendance.repository;

import com.inchinso.back.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUserId(Long userId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.user.id = :userId AND a.attendedAt >= :from")
    int countByUserIdAndAttendedAtAfter(@Param("userId") Long userId, @Param("from") LocalDateTime from);

    List<Attendance> findBySessionId(Long sessionId);
}
