package io.github.junhyoung.nearbuy.auth.token.service;

import io.github.junhyoung.nearbuy.auth.web.util.JWTUtil;
import io.github.junhyoung.nearbuy.auth.token.dto.request.RefreshRequestDto;
import io.github.junhyoung.nearbuy.auth.token.dto.response.JWTResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new RuntimeException("쿠키가 존재하지 않습니다.");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null) {
            throw new RuntimeException("refreshToken 쿠키가 없습니다.");
        }

        if (!JWTUtil.isValid(refreshToken, false)) {
            throw new RuntimeException("유효하지 않은 refreshToken입니다.");
        }

        Long id = JWTUtil.getId(refreshToken);
        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        String newAccessToken = JWTUtil.createJWT(id, username, role, true);
        String newRefreshToken = JWTUtil.createJWT(id, username, role, false);

        // Redis에서 기존 토큰 삭제 후 신규 토큰 저장 (Rotate)
        removeRefresh(refreshToken);
        addRefresh(username, newRefreshToken);

        // 기존 쿠키 제거
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(refreshCookie);

        return new JWTResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    public JWTResponseDto refreshRotate(RefreshRequestDto dto) {
        String refreshToken = dto.getRefreshToken();

        if (!JWTUtil.isValid(refreshToken, false)) {
            throw new RuntimeException("유효하지 않은 refreshToken입니다.");
        }

        // Redis에 해당 토큰이 존재하는지 확인
        if (Boolean.FALSE.equals(existsRefresh(refreshToken))) {
            throw new RuntimeException("DB에 존재하지 않는 유효하지 않은 refreshToken입니다.");
        }

        Long id = JWTUtil.getId(refreshToken);
        String username = JWTUtil.getUsername(refreshToken);
        String role = JWTUtil.getRole(refreshToken);

        String newAccessToken = JWTUtil.createJWT(id, username, role, true);
        String newRefreshToken = JWTUtil.createJWT(id, username, role, false);

        // Redis에서 기존 토큰 삭제 후 신규 토큰 저장 (Rotate)
        removeRefresh(refreshToken);
        addRefresh(username, newRefreshToken);

        return new JWTResponseDto(newAccessToken, newRefreshToken);
    }

    public Boolean existsRefresh(String refreshToken) {
        String username = JWTUtil.getUsername(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
        return refreshToken.equals(storedToken);
    }

    @Transactional
    public void addRefresh(String username, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + username,
                refreshToken,
                REFRESH_TOKEN_EXPIRATION_DAYS,
                TimeUnit.DAYS
        );
    }

    @Transactional
    public void removeRefresh(String refreshToken) {
        String username = JWTUtil.getUsername(refreshToken);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    }

    @Transactional
    public void removeRefreshUser(String username) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
    }
}