package com.inchinso.back.domain.participation.service;

import com.inchinso.back.domain.participation.entity.Participation;
import com.inchinso.back.domain.participation.entity.ParticipationStatus;
import com.inchinso.back.domain.participation.repository.ParticipationRepository;
import com.inchinso.back.domain.session.entity.BadmintonSession;
import com.inchinso.back.domain.session.entity.SessionStatus;
import com.inchinso.back.domain.session.repository.BadmintonSessionRepository;
import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.domain.user.repository.UserRepository;
import com.inchinso.back.global.exception.CustomException;
import com.inchinso.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final BadmintonSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * 참가 신청 - 동시성 처리 (비관적 락)
     */
    @Transactional
    public void apply(Long userId, Long sessionId) {
        BadmintonSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.CLOSED ||
            session.getStatus() == SessionStatus.CANCELLED) {
            throw new CustomException(ErrorCode.SESSION_CLOSED);
        }

        // 중복 신청 확인
        Optional<Participation> existing =
                participationRepository.findBySessionIdAndUserId(sessionId, userId);

        if (existing.isPresent()) {
            if (existing.get().getStatus() == ParticipationStatus.CONFIRMED) {
                throw new CustomException(ErrorCode.ALREADY_PARTICIPATED);
            }
            // 취소했다가 재신청하는 경우
        }

        // 정원 확인 (select for update로 동시성 처리)
        int currentCount = participationRepository.countConfirmedBySessionId(sessionId);
        if (currentCount >= session.getMaxParticipants()) {
            throw new CustomException(ErrorCode.SESSION_FULL);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (existing.isPresent()) {
            // 재신청: 상태만 CONFIRMED로 (cancel 후 재신청)
            // 실제로는 새로 저장
            participationRepository.delete(existing.get());
        }

        Participation participation = Participation.builder()
                .session(session)
                .user(user)
                .build();
        participationRepository.save(participation);
    }

    /**
     * 참가 취소
     */
    @Transactional
    public void cancel(Long userId, Long sessionId) {
        Participation participation = participationRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        if (participation.getStatus() != ParticipationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.NOT_PARTICIPATED);
        }

        participation.cancel();
    }

    /**
     * 내 신청 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isApplied(Long userId, Long sessionId) {
        return participationRepository.findBySessionIdAndUserId(sessionId, userId)
                .map(p -> p.getStatus() == ParticipationStatus.CONFIRMED)
                .orElse(false);
    }
}
