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
        BadmintonSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        if (session.getStatus() == SessionStatus.CLOSED ||
            session.getStatus() == SessionStatus.CANCELLED) {
            throw new CustomException(ErrorCode.SESSION_CLOSED);
        }

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
            int currentCount = participationRepository.countConfirmedBySessionId(sessionId);
            if (currentCount >= session.getMaxParticipants()) {
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
        sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        Participation participation = participationRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPATION_NOT_FOUND));

        if (participation.getStatus() == ParticipationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.NOT_PARTICIPATED);
        }

        boolean wasConfirmed = participation.getStatus() == ParticipationStatus.CONFIRMED;
        participation.cancel();

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
                    p.getStatus().name()  // 대문자로 원상복귀
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
