package com.inchinso.back.domain.participation.controller;

import com.inchinso.back.domain.participation.service.ParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Participation", description = "모임 참가 신청 API")
@RestController
@RequestMapping("/api/sessions/{sessionId}/participations")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @Operation(summary = "참가 신청")
    @PostMapping
    public ResponseEntity<Void> apply(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {
        participationService.apply(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "참가 취소")
    @DeleteMapping
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {
        participationService.cancel(userId, sessionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 신청 여부 확인")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Boolean>> isApplied(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {
        boolean applied = participationService.isApplied(userId, sessionId);
        return ResponseEntity.ok(Map.of("applied", applied));
    }

    @Operation(summary = "참가자 목록 조회 (일반 회원용)",
            description = "본인 이름은 표시, 다른 참가자는 익명(참가자 N)으로 표시. isMe=true인 항목이 본인.")
    @GetMapping("/public")
    public ResponseEntity<List<ParticipationService.ParticipantPublicResponse>> getParticipantsPublic(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(participationService.getParticipantsPublic(sessionId, userId));
    }
}
