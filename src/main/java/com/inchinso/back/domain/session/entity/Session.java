package com.inchinso.back.domain.session.entity;

import com.inchinso.back.domain.club.entity.Club;
import com.inchinso.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // 운영진

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String rules; // 수칙

    @Column(nullable = false)
    private int maxParticipants; // 기본 16

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void updateMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void updateStatus(SessionStatus status) {
        this.status = status;
    }

    public void update(LocalDate sessionDate, LocalTime startTime, LocalTime endTime,
                       String location, String rules, int maxParticipants) {
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.rules = rules;
        this.maxParticipants = maxParticipants;
    }
}
