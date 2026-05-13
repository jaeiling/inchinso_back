package com.inchinso.back.domain.user.controller;

import com.inchinso.back.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User", description = "회원 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserService.MyInfoResponse> getMyInfo(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getMyInfo(userId));
    }

    @Operation(summary = "내 이름 수정")
    @PatchMapping("/me")
    public ResponseEntity<Void> updateMyName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateNameRequest req) {
        userService.updateMyName(userId, req.name());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "전체 회원 목록 조회 (운영진)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserService.MemberResponse>> getAllMembers() {
        return ResponseEntity.ok(userService.getAllMembers());
    }

    @Operation(summary = "회원 삭제 (운영진)")
    @DeleteMapping("/{targetUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long targetUserId) {
        userService.deactivateUser(adminId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "권한 승계 - 현재 운영진 → 대상 회원 (운영진)")
    @PostMapping("/{targetUserId}/transfer-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> transferAdmin(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long targetUserId) {
        userService.transferAdmin(adminId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "운영진 권한 부여 (운영진)")
    @PostMapping("/{targetUserId}/grant-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> grantAdmin(@PathVariable Long targetUserId) {
        userService.grantAdmin(targetUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "운영진 권한 해제 (운영진)")
    @PostMapping("/{targetUserId}/revoke-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeAdmin(@PathVariable Long targetUserId) {
        userService.revokeAdmin(targetUserId);
        return ResponseEntity.ok().build();
    }

    public record UpdateNameRequest(
            @NotBlank(message = "이름을 입력해주세요.") String name
    ) {}
}
