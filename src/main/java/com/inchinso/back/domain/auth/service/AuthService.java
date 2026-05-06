package com.inchinso.back.domain.auth.service;

import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.domain.user.repository.UserRepository;
import com.inchinso.back.global.exception.CustomException;
import com.inchinso.back.global.exception.ErrorCode;
import com.inchinso.back.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Transactional
    public void registerName(Long userId, String name) {
        if (!StringUtils.hasText(name)) {
            throw new CustomException(ErrorCode.NAME_REQUIRED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateName(name.trim());
    }

    @Transactional(readOnly = true)
    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        String stored = redisTemplate.opsForValue().get("refresh:" + userId);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(userId, user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);

        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                newRefreshToken,
                jwtProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logout(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    public record TokenResponse(String accessToken, String refreshToken) {}
}
