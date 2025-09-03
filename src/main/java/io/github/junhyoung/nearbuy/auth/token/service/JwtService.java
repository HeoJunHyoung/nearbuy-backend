package io.github.junhyoung.nearbuy.auth.token.service;

import io.github.junhyoung.nearbuy.auth.web.util.JWTUtil;
import io.github.junhyoung.nearbuy.auth.token.dto.request.RefreshRequestDto;
import io.github.junhyoung.nearbuy.auth.token.dto.response.JWTResponseDto;
import io.github.junhyoung.nearbuy.global.exception.ErrorCode;
import io.github.junhyoung.nearbuy.global.exception.business.InvalidRefreshTokenException;
import io.github.junhyoung.nearbuy.global.exception.business.RefreshTokenNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    // 소셜 로그인 성공 후 쿠키(Refresh) -> 헤더 방식으로 응답
    @Transactional
    public JWTResponseDto cookie2Header(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RefreshTokenNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RefreshTokenNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 공통 로직을 호출하여 토큰 재발급
        JWTResponseDto newTokens = rotateTokens(refreshToken);

        // 기존 쿠키 제거
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(refreshCookie);

        return newTokens;
    }

    @Transactional
    public JWTResponseDto refreshRotate(RefreshRequestDto dto) {
        // 공통 로직을 호출하여 토큰 재발급
        return rotateTokens(dto.getRefreshToken());
    }

    /**
     * Refresh Token을 검증하고 새로운 Access/Refresh Token을 발급 (Rotate)하는 공통 메서드
     */
    private JWTResponseDto rotateTokens(String refreshToken) {
        // 유효성 검사와 DB 존재 여부 확인을 한 번에 처리
        if (!JWTUtil.isValid(refreshToken, false) || Boolean.FALSE.equals(existsRefresh(refreshToken))) {
            throw new InvalidRefreshTokenException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long id = JWTUtil.getId(refreshToken);
        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        String newAccessToken = JWTUtil.createJWT(id, username, role, true);
        String newRefreshToken = JWTUtil.createJWT(id, username, role, false);

        // Redis에서 기존 토큰 삭제 후 신규 토큰 저장 (Rotate)
        removeRefresh(refreshToken);
        addRefresh(id, newRefreshToken);

        return new JWTResponseDto(newAccessToken, newRefreshToken);
    }

    public Boolean existsRefresh(String refreshToken) {
        Long userId = JWTUtil.getId(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        return refreshToken.equals(storedToken);
    }

    // userId를 키로 사용
    @Transactional
    public void addRefresh(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                REFRESH_TOKEN_EXPIRATION_DAYS,
                TimeUnit.DAYS
        );
    }

    // 토큰에서 userId를 추출하여 삭제
    @Transactional
    public void removeRefresh(String refreshToken) {
        Long userId = JWTUtil.getId(refreshToken);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    // 회원 탈퇴 시 userId로 직접 삭제
    @Transactional
    public void removeRefreshUser(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}