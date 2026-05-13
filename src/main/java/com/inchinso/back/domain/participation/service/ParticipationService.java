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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final BadmintonSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void apply(Long userId, Long sessionId) {
        // 비관적 락으로 세션 조회 - 동시 신청 방지
        BadmintonSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.CLOSED ||
            session.getStatus() == SessionStatus.CANCELLED) {
            throw new CustomException(ErrorCode.SESSION_CLOSED);
        }

        // 오픈 시간 체크 (null이면 즉시 오픈)
        if (session.getOpenAt() != null &&
            LocalDateTime.now().isBefore(session.getOpenAt())) {
            throw new CustomException(ErrorCode.PARTICIPATION_NOT_OPEN);
        }

        Optional<Participation> existing =
                participationRepository.findBySessionIdAndUserId(sessionId, userId);

        if (existing.isPresent()) {
            Participation p = existing.get();
            if (p.getStatus() == ParticipationStatus.CONFIRMED ||
                p.getStatus() == ParticipationStatus.WAITING) {
                throw new CustomException(ErrorCode.ALREADY_PARTICIPATED);
            }
            // 취소 후 재신청
            int currentCount = participationRepository.countConfirmedBySessionId(sessionId);
            if (currentCount >= session.getMaxParticipants()) {
                // 정원 초과 → 대기자로 재신청
                p.reactivateAsWaiting();
            } else {
                p.reactivate();
            }
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        int currentCount = participationRepository.countConfirmedBySessionId(sessionId);
        ParticipationStatus status = currentCount >= session.getMaxParticipants()
                ? ParticipationStatus.WAITING
                : ParticipationStatus.CONFIRMED;

        participationRepository.save(Participation.builder()
                .session(session)
                .user(user)
                .status(status)
                .build());
    }

    @Transactional
    public void cancel(Long userId, Long sessionId) {
        // 비관적 락으로 세션 조회 - 취소 시 대기자 승격 동시성 처리
        BadmintonSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        Participation participation = participationRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        if (participation.getStatus() == ParticipationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.NOT_PARTICIPATED);
        }

        boolean wasConfirmed = participation.getStatus() == ParticipationStatus.CONFIRMED;
        participation.cancel();

        // 확정 참가자가 취소한 경우 → 대기자 1순위 자동 승격
        if (wasConfirmed) {
            List<Participation> waiting =
                    participationRepository.findWaitingBySessionIdOrderByCreatedAt(sessionId);
            if (!waiting.isEmpty()) {
                waiting.get(0).promoteToConfirmed();
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isApplied(Long userId, Long sessionId) {
        return participationRepository.findBySessionIdAndUserId(sessionId, userId)
                .map(p -> p.getStatus() == ParticipationStatus.CONFIRMED ||
                          p.getStatus() == ParticipationStatus.WAITING)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<ParticipantPublicResponse> getParticipantsPublic(Long sessionId, Long myUserId) {
        // CONFIRMED 먼저, 그 다음 WAITING 순으로 조회
        List<Participation> active = participationRepository.findActiveBySessionId(sessionId);

        List<ParticipantPublicResponse> result = new ArrayList<>();
        int confirmedOrder = 1;
        int waitingOrder = 1;

        for (Participation p : active) {
            boolean isMe = p.getUser().getId().equals(myUserId);
            boolean isWaiting = p.getStatus() == ParticipationStatus.WAITING;

            int order = isWaiting ? waitingOrder++ : confirmedOrder++;

            result.add(new ParticipantPublicResponse(
                    order,
                    isMe ? p.getUser().getId() : null,
                    isMe ? p.getUser().getName() : null,
                    isMe,
                    p.getStatus().name()
            ));
        }
        return result;
    }

    public record ParticipantPublicResponse(
            int order,
            Long userId,
            String userName,
            boolean isMe,
            String status
    ) {}
}
