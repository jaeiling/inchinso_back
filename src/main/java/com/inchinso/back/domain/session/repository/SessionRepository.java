package com.inchinso.back.domain.session.repository;

import com.inchinso.back.domain.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByClubIdAndSessionDateBetween(Long clubId, LocalDate start, LocalDate end);
    List<Session> findByClubIdOrderBySessionDateAsc(Long clubId);
}
