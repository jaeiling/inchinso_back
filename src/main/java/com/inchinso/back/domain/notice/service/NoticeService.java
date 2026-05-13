package com.inchinso.back.domain.notice.service;

import com.inchinso.back.domain.club.entity.Club;
import com.inchinso.back.domain.club.repository.ClubRepository;
import com.inchinso.back.domain.notice.entity.Notice;
import com.inchinso.back.domain.notice.entity.NoticeImage;
import com.inchinso.back.domain.notice.repository.NoticeRepository;
import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.domain.user.repository.UserRepository;
import com.inchinso.back.global.exception.CustomException;
import com.inchinso.back.global.exception.ErrorCode;
import com.inchinso.back.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public List<NoticeResponse> getNotices(Long clubId) {
        return noticeRepository.findByClubIdOrderByPinnedDescCreatedAtDesc(clubId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        return toResponse(getNoticeById(noticeId));
    }

    @Transactional
    public Long createNotice(Long authorId, Long clubId, String title, String content,
                             boolean pinned, List<MultipartFile> images) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLUB_NOT_FOUND));

        Notice notice = Notice.builder()
                .club(club)
                .author(author)
                .title(title)
                .content(content)
                .pinned(pinned)
                .build();

        noticeRepository.save(notice);

        if (images != null && !images.isEmpty()) {
            uploadImages(notice, images);
        }

        return notice.getId();
    }

    @Transactional
    public void updateNotice(Long noticeId, String title, String content, boolean pinned,
                             List<MultipartFile> newImages) {
        Notice notice = getNoticeById(noticeId);
        notice.update(title, content, pinned);

        // 기존 이미지 유지 + 새 이미지 추가
        if (newImages != null && !newImages.isEmpty()) {
            uploadImages(notice, newImages);
        }
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = getNoticeById(noticeId);
        notice.getImages().forEach(img -> s3Service.delete(img.getS3Key()));
        noticeRepository.delete(notice);
    }

    private void uploadImages(Notice notice, List<MultipartFile> images) {
        List<NoticeImage> noticeImages = new ArrayList<>();
        int startOrder = notice.getImages().size(); // 기존 이미지 개수 이후부터 순서 부여
        for (int i = 0; i < images.size(); i++) {
            String url = s3Service.upload(images.get(i), "notices");
            String key = s3Service.getKey(url);
            noticeImages.add(NoticeImage.builder()
                    .notice(notice)
                    .imageUrl(url)
                    .s3Key(key)
                    .sortOrder(startOrder + i)
                    .build());
        }
        notice.getImages().addAll(noticeImages);
    }

    private Notice getNoticeById(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
    }

    private NoticeResponse toResponse(Notice n) {
        List<String> imageUrls = n.getImages().stream()
                .map(NoticeImage::getImageUrl)
                .toList();
        return new NoticeResponse(
                n.getId(), n.getTitle(), n.getContent(), n.isPinned(),
                n.getAuthor().getName(), n.getCreatedAt(), imageUrls
        );
    }

    public record NoticeResponse(
            Long id, String title, String content, boolean pinned,
            String authorName, LocalDateTime createdAt, List<String> imageUrls
    ) {}
}
