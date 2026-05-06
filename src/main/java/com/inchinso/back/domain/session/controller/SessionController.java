package com.inchinso.back.domain.session.controller;

import com.inchinso.back.domain.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Session", description = "모임 일정 API")
@RestController
@RequestMapping("/api/clubs/{clubId}/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "월별 모임 목록 조회")
    @GetMapping
    public ResponseEntity<List<SessionService.SessionSummaryResponse>> getSessions(
            @PathVariable Long clubId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(sessionService.getSessionsByMonth(clubId, year, month));
    }

    @Operation(summary = "모임 상세 조회")
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionService.SessionDetailResponse> getSession(
            @PathVariable Long clubId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getSessionDetail(sessionId));
    }

    @Operation(summary = "모임 생성 (운영진)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createSession(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long clubId,
            @RequestBody SessionService.SessionCreateRequest req) {
        return ResponseEntity.ok(sessionService.createSession(adminId, clubId, req));
    }

    @Operation(summary = "모임 수정 (운영진)")
    @PutMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateSession(
            @PathVariable Long clubId,
            @PathVariable Long sessionId,
            @RequestBody SessionService.SessionCreateRequest req) {
        sessionService.updateSession(sessionId, req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모임 삭제 (운영진)")
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Long clubId,
            @PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "정원 수정 (운영진)")
    @PatchMapping("/{sessionId}/max-participants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateMaxParticipants(
            @PathVariable Long clubId,
            @PathVariable Long sessionId,
            @RequestParam int max) {
        sessionService.updateMaxParticipants(sessionId, max);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "참가자 명단 조회 (운영진)")
    @GetMapping("/{sessionId}/participants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionService.ParticipantResponse>> getParticipants(
            @PathVariable Long clubId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getParticipants(sessionId));
    }
}
