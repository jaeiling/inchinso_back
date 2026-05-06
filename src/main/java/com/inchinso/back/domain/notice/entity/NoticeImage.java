package com.inchinso.back.domain.notice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class NoticeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(nullable = false)
    private String imageUrl; // S3 URL

    @Column(nullable = false)
    private String s3Key; // S3 삭제용 키

    @Column(nullable = false)
    private int sortOrder;
}
