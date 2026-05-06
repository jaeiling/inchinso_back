package com.inchinso.back.global.security.oauth2;

import com.inchinso.back.domain.user.entity.User;
import com.inchinso.back.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                jwtProvider.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        String redirectUrl;
        if (oAuth2User.isNewUser()) {
            redirectUrl = frontendUrl + "/onboarding?token=" + accessToken + "&refresh=" + refreshToken;
        } else {
            redirectUrl = frontendUrl + "/home?token=" + accessToken + "&refresh=" + refreshToken;
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
