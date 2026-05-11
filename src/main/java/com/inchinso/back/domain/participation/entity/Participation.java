package com.inchinso.back.domain.participation.entity;

import com.inchinso.back.domain.session.entity.BadmintonSession;
import com.inchinso.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "participations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private BadmintonSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ParticipationStatus status = ParticipationStatus.CONFIRMED;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void cancel() {
        this.status = ParticipationStatus.CANCELLED;
    }

    public void reactivate() {
        this.status = ParticipationStatus.CONFIRMED;
    }
}
