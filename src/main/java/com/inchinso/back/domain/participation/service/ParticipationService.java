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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final BadmintonSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void apply(Long userId, Long sessionId) {
        BadmintonSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.CLOSED ||
            session.getStatus() == SessionStatus.CANCELLED) {
            throw new CustomException(ErrorCode.SESSION_CLOSED);
        }

        Optional<Participation> existing =
                participationRepository.findBySessionIdAndUserId(sessionId, userId);

        if (existing.isPresent()) {
            Participation p = existing.get();
            if (p.getStatus() == ParticipationStatus.CONFIRMED) {
                // 이미 신청 완료 상태
                throw new CustomException(ErrorCode.ALREADY_PARTICIPATED);
            }
            // 취소했다가 재신청: status만 CONFIRMED로 업데이트 (새로 insert 안 함)
            int currentCount = participationRepository.countConfirmedBySessionId(sessionId);
            if (currentCount >= session.getMaxParticipants()) {
                throw new CustomException(ErrorCode.SESSION_FULL);
            }
            p.reactivate();
            return;
        }

        // 최초 신청
        int currentCount = participationRepository.countConfirmedBySessionId(sessionId);
        if (currentCount >= session.getMaxParticipants()) {
            throw new CustomException(ErrorCode.SESSION_FULL);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Participation participation = Participation.builder()
                .session(session)
                .user(user)
                .build();
        participationRepository.save(participation);
    }

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

    @Transactional(readOnly = true)
    public boolean isApplied(Long userId, Long sessionId) {
        return participationRepository.findBySessionIdAndUserId(sessionId, userId)
                .map(p -> p.getStatus() == ParticipationStatus.CONFIRMED)
                .orElse(false);
    }
}
