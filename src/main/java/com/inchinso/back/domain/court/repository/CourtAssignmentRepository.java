package com.inchinso.back.domain.court.repository;

import com.inchinso.back.domain.court.entity.CourtAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtAssignmentRepository extends JpaRepository<CourtAssignment, Long> {
    List<CourtAssignment> findBySessionId(Long sessionId);
    void deleteBySessionId(Long sessionId);
}
