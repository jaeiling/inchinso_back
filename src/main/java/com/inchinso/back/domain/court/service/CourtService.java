package com.inchinso.back.domain.court.service;

import com.inchinso.back.domain.court.entity.CourtAssignment;
import com.inchinso.back.domain.court.entity.MatchType;
import com.inchinso.back.domain.court.repository.CourtAssignmentRepository;
import com.inchinso.back.domain.participation.entity.ParticipationStatus;
import com.inchinso.back.domain.participation.repository.ParticipationRepository;
import com.inchinso.back.domain.session.entity.BadmintonSession;
import com.inchinso.back.domain.session.repository.BadmintonSessionRepository;
import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.domain.user.repository.UserRepository;
import com.inchinso.back.global.exception.CustomException;
import com.inchinso.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtAssignmentRepository courtAssignmentRepository;
    private final ParticipationRepository participationRepository;
    private final BadmintonSessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CourtAssignmentResponse> getAssignments(Long sessionId) {
        return courtAssignmentRepository.findBySessionId(sessionId)
                .stream()
                .map(a -> new CourtAssignmentResponse(
                        a.getId(), a.getUser().getId(), a.getUser().getName(),
                        a.getCourtNumber(), a.getMatchType().name()
                ))
                .toList();
    }

    @Transactional
    public void assign(Long sessionId, List<AssignRequest> requests) {
        BadmintonSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        courtAssignmentRepository.deleteBySessionId(sessionId);

        List<Long> confirmedUserIds = participationRepository
                .findBySessionIdAndStatus(sessionId, ParticipationStatus.CONFIRMED)
                .stream()
                .map(p -> p.getUser().getId())
                .toList();

        for (AssignRequest req : requests) {
            if (req.courtNumber() < 1 || req.courtNumber() > 5) {
                throw new CustomException(ErrorCode.INVALID_COURT_NUMBER);
            }
            if (!confirmedUserIds.contains(req.userId())) {
                throw new CustomException(ErrorCode.USER_NOT_IN_SESSION);
            }
            User user = userRepository.findById(req.userId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            courtAssignmentRepository.save(CourtAssignment.builder()
                    .session(session)
                    .user(user)
                    .courtNumber(req.courtNumber())
                    .matchType(MatchType.valueOf(req.matchType()))
                    .build());
        }
    }

    @Transactional
    public void clearAssignments(Long sessionId) {
        courtAssignmentRepository.deleteBySessionId(sessionId);
    }

    public record AssignRequest(Long userId, int courtNumber, String matchType) {}
    public record CourtAssignmentResponse(Long id, Long userId, String userName, int courtNumber, String matchType) {}
}
