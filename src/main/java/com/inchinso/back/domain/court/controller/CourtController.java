package com.inchinso.back.domain.court.controller;

import com.inchinso.back.domain.court.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Court", description = "코트 배정 API")
@RestController
@RequestMapping("/api/sessions/{sessionId}/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    @Operation(summary = "코트 배정 목록 조회")
    @GetMapping
    public ResponseEntity<List<CourtService.CourtAssignmentResponse>> getAssignments(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(courtService.getAssignments(sessionId));
    }

    @Operation(summary = "코트 배정 저장 (운영진)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assign(
            @PathVariable Long sessionId,
            @RequestBody List<CourtService.AssignRequest> requests) {
        courtService.assign(sessionId, requests);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "코트 배정 초기화 (운영진)")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearAssignments(@PathVariable Long sessionId) {
        courtService.clearAssignments(sessionId);
        return ResponseEntity.noContent().build();
    }
}
