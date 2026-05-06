package com.inchinso.back.domain.participation.repository;

import com.inchinso.back.domain.participation.entity.Participation;
import com.inchinso.back.domain.participation.entity.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    Optional<Participation> findBySessionIdAndUserId(Long sessionId, Long userId);

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.session.id = :sessionId AND p.status = 'CONFIRMED'")
    int countConfirmedBySessionId(@Param("sessionId") Long sessionId);

    List<Participation> findBySessionIdAndStatus(Long sessionId, ParticipationStatus status);

    List<Participation> findByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM Participation p WHERE p.user.id = :userId AND p.status = 'CONFIRMED'")
    int countTotalByUserId(@Param("userId") Long userId);
}
