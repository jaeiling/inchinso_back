package com.inchinso.back.domain.court.entity;

import com.inchinso.back.domain.session.entity.BadmintonSession;
import com.inchinso.back.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "court_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class CourtAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private BadmintonSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int courtNumber; // 1~5

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private MatchType matchType = MatchType.DOUBLES;

    public void updateCourt(int courtNumber, MatchType matchType) {
        this.courtNumber = courtNumber;
        this.matchType = matchType;
    }
}
