package com.inchinso.back.domain.session.repository;

import com.inchinso.back.domain.session.entity.BadmintonSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BadmintonSessionRepository extends JpaRepository<BadmintonSession, Long> {

    List<BadmintonSession> findByClubIdAndSessionDateBetweenOrderBySessionDateAsc(
            Long clubId, LocalDate start, LocalDate end);

    List<BadmintonSession> findByClubIdOrderBySessionDateDesc(Long clubId);

    // 비관적 락 - 참가 신청 동시성 처리용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BadmintonSession s WHERE s.id = :id")
    Optional<BadmintonSession> findByIdWithLock(@Param("id") Long id);
}
