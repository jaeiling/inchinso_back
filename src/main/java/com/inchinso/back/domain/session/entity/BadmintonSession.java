package com.inchinso.back.domain.session.entity;

import com.inchinso.back.domain.club.entity.Club;
import com.inchinso.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "badminton_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class BadmintonSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Builder.Default
    @Column(nullable = false)
    private int maxParticipants = 16;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.OPEN;

    @Column
    private LocalDateTime openAt; // 참가 신청 오픈 시간 (null이면 즉시 오픈)

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void updateMaxParticipants(int max) {
        this.maxParticipants = max;
    }

    public void updateStatus(SessionStatus status) {
        this.status = status;
    }

    public void update(LocalDate date, LocalTime startTime, LocalTime endTime,
                       String location, String rules, int maxParticipants,
                       LocalDateTime openAt) {
        this.sessionDate = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.rules = rules;
        this.maxParticipants = maxParticipants;
        this.openAt = openAt;
    }
}
