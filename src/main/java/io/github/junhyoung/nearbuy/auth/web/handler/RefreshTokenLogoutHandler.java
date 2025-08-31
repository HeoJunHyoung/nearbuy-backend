package io.github.junhyoung.nearbuy.auth.web.handler;

import io.github.junhyoung.nearbuy.auth.web.dto.LogoutRequestDto;
import io.github.junhyoung.nearbuy.auth.web.util.JWTUtil;
import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            // 1. DTO를 사용하여 요청 본문을 직접 파싱
            LogoutRequestDto logoutRequest = objectMapper.readValue(request.getInputStream(), LogoutRequestDto.class);
            String refreshToken = logoutRequest.getRefreshToken();

            // 2. 토큰 존재 여부 및 유효성 검증 로직을 명확하게 개선
            if (StringUtils.hasText(refreshToken) && JWTUtil.isValid(refreshToken, false)) {
                // 3. Refresh 토큰 삭제
                jwtService.removeRefresh(refreshToken);
            } else {
                // 4. 유효하지 않은 토큰에 대한 로그를 남겨 추적 용이성을 높임
                log.warn("Invalid or missing refresh token on logout attempt.");
            }

        } catch (IOException e) {
            // 5. 파싱 실패 시 로그를 남김. 로그아웃은 실패해도 심각한 문제가 아니므로 예외를 던지지 않을 수 있음.
            log.error("Failed to parse logout request body.", e);
        }
    }

}