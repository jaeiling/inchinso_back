package com.inchinso.back.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name; // 본명 (최초 로그인 시 입력)

    @Column(nullable = false)
    private String provider; // "google"

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private String profileImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true; // 운영진이 삭제 시 false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private com.inchinso.back.domain.club.entity.Club club;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void deactivate() {
        this.active = false;
    }

    public void assignClub(com.inchinso.back.domain.club.entity.Club club) {
        this.club = club;
    }
}
