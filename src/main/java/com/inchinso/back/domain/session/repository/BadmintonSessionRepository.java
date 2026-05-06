package com.inchinso.back.domain.session.repository;

import com.inchinso.back.domain.session.entity.BadmintonSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BadmintonSessionRepository extends JpaRepository<BadmintonSession, Long> {
    List<BadmintonSession> findByClubIdAndSessionDateBetweenOrderBySessionDateAsc(
            Long clubId, LocalDate start, LocalDate end);
    List<BadmintonSession> findByClubIdOrderBySessionDateDesc(Long clubId);
}
