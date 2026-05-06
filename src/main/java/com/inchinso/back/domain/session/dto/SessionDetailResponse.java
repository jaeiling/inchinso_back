package com.inchinso.back.domain.session.dto;

import com.inchinso.back.domain.session.entity.Session;
import com.inchinso.back.domain.session.entity.SessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class SessionDetailResponse {
    private Long id;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String rules;
    private int maxParticipants;
    private int currentParticipants;
    private SessionStatus status;
    private boolean isParticipating; // 현재 유저 신청 여부

    // 운영진만 볼 수 있는 필드
    private List<ParticipantInfo> participants; // null이면 회원

    @Getter
    @Builder
    public static class ParticipantInfo {
        private Long userId;
        private String name;
    }
}
