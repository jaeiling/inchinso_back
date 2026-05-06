package com.inchinso.back.domain.session.service;

import com.inchinso.back.domain.club.entity.Club;
import com.inchinso.back.domain.club.repository.ClubRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final BadmintonSessionRepository sessionRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public List<SessionSummaryResponse> getSessionsByMonth(Long clubId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return sessionRepository
                .findByClubIdAndSessionDateBetweenOrderBySessionDateAsc(clubId, start, end)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(Long sessionId) {
        BadmintonSession session = getSession(sessionId);
        int confirmed = participationRepository.countConfirmedBySessionId(sessionId);
        return toDetail(session, confirmed);
    }

    @Transactional
    public Long createSession(Long adminId, Long clubId, SessionCreateRequest req) {
        User admin = getUser(adminId);
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        BadmintonSession session = BadmintonSession.builder()
                .club(club)
                .createdBy(admin)
                .sessionDate(req.sessionDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .location(req.location())
                .rules(req.rules())
                .maxParticipants(req.maxParticipants() > 0 ? req.maxParticipants() : 16)
                .build();

        return sessionRepository.save(session).getId();
    }

    @Transactional
    public void updateSession(Long sessionId, SessionCreateRequest req) {
        BadmintonSession session = getSession(sessionId);
        session.update(req.sessionDate(), req.startTime(), req.endTime(),
                req.location(), req.rules(), req.maxParticipants());
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        sessionRepository.delete(getSession(sessionId));
    }

    @Transactional
    public void updateMaxParticipants(Long sessionId, int max) {
        getSession(sessionId).updateMaxParticipants(max);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(Long sessionId) {
        return participationRepository
                .findBySessionIdAndStatus(sessionId, ParticipationStatus.CONFIRMED)
                .stream()
                .map(p -> new ParticipantResponse(
                        p.getUser().getId(),
                        p.getUser().getName(),
                        p.getCreatedAt()
                ))
                .toList();
    }

    private BadmintonSession getSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private SessionSummaryResponse toSummary(BadmintonSession s) {
        int confirmed = participationRepository.countConfirmedBySessionId(s.getId());
        return new SessionSummaryResponse(
                s.getId(), s.getSessionDate(), s.getStartTime(), s.getEndTime(),
                s.getLocation(), s.getMaxParticipants(), confirmed, s.getStatus().name()
        );
    }

    private SessionDetailResponse toDetail(BadmintonSession s, int confirmed) {
        return new SessionDetailResponse(
                s.getId(), s.getSessionDate(), s.getStartTime(), s.getEndTime(),
                s.getLocation(), s.getRules(), s.getMaxParticipants(), confirmed, s.getStatus().name()
        );
    }

    public record SessionCreateRequest(
            LocalDate sessionDate, LocalTime startTime, LocalTime endTime,
            String location, String rules, int maxParticipants
    ) {}

    public record SessionSummaryResponse(
            Long id, LocalDate sessionDate, LocalTime startTime, LocalTime endTime,
            String location, int maxParticipants, int confirmedCount, String status
    ) {}

    public record SessionDetailResponse(
            Long id, LocalDate sessionDate, LocalTime startTime, LocalTime endTime,
            String location, String rules, int maxParticipants, int confirmedCount, String status
    ) {}

    public record ParticipantResponse(Long userId, String name, LocalDateTime appliedAt) {}
}
