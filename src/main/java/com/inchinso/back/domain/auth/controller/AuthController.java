package com.inchinso.back.domain.auth.controller;

import com.inchinso.back.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "온보딩 - 이름 등록", description = "최초 로그인 시 본명 등록")
    @PostMapping("/onboarding")
    public ResponseEntity<Void> registerName(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody OnboardingRequest req) {
        authService.registerName(userId, req.name());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<AuthService.TokenResponse> reissue(
            @RequestBody ReissueRequest req) {
        return ResponseEntity.ok(authService.reissue(req.refreshToken()));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

    public record OnboardingRequest(@NotBlank(message = "이름을 입력해주세요.") String name) {}
    public record ReissueRequest(@NotBlank String refreshToken) {}
}
