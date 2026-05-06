package com.inchinso.back.domain.notice.controller;

import com.inchinso.back.domain.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequestMapping("/api/clubs/{clubId}/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 목록 조회")
    @GetMapping
    public ResponseEntity<List<NoticeService.NoticeResponse>> getNotices(
            @PathVariable Long clubId) {
        return ResponseEntity.ok(noticeService.getNotices(clubId));
    }

    @Operation(summary = "공지사항 단건 조회")
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeService.NoticeResponse> getNotice(
            @PathVariable Long clubId,
            @PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

    @Operation(summary = "공지사항 작성 (운영진)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createNotice(
            @AuthenticationPrincipal Long authorId,
            @PathVariable Long clubId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean pinned,
            @RequestPart(required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(
                noticeService.createNotice(authorId, clubId, title, content, pinned, images));
    }

    @Operation(summary = "공지사항 수정 (운영진)")
    @PutMapping(value = "/{noticeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateNotice(
            @PathVariable Long clubId,
            @PathVariable Long noticeId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean pinned,
            @RequestPart(required = false) List<MultipartFile> images) {
        noticeService.updateNotice(noticeId, title, content, pinned, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "공지사항 삭제 (운영진)")
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long clubId,
            @PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}
