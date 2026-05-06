package com.inchinso.back.domain.session.dto;

import com.inchinso.back.domain.session.entity.Session;
import com.inchinso.back.domain.session.entity.SessionStatus;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class SessionSummaryResponse {
    private final Long id;
    private final LocalDate sessionDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;
    private final int maxParticipants;
    private final int currentParticipants;
    private final SessionStatus status;

    public SessionSummaryResponse(Session session, int currentParticipants) {
        this.id = session.getId();
        this.sessionDate = session.getSessionDate();
        this.startTime = session.getStartTime();
        this.endTime = session.getEndTime();
        this.location = session.getLocation();
        this.maxParticipants = session.getMaxParticipants();
        this.currentParticipants = currentParticipants;
        this.status = session.getStatus();
    }
}
