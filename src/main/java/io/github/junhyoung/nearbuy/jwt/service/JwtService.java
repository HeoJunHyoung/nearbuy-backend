package io.github.junhyoung.nearbuy.jwt.service;

import io.github.junhyoung.nearbuy.jwt.entity.RefreshToken;
import io.github.junhyoung.nearbuy.jwt.repository.RefreshRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtService {

    private final RefreshRepository refreshRepository;

    // 소셜 로그인 성공 후 쿠키(Refresh) -> 헤더 방식으로 응답 <-- 이건 추후에 작성

    // Refresh 토큰으로 Access 토큰 재발급 로직 (Rotate 포함) <-- 이건 추후에 작성

    // JWT Refresh 토큰 발급 후 저장 메소드
    @Transactional
    public void addRefresh(String username, String refreshToken) {
        RefreshToken.builder()
                .username(username)
                .refresh(refreshToken)
                .build();
    }

    // JWT Refresh 존재 확인 메소드
    public Boolean existsRefresh(String username) {
        return refreshRepository.existsByRefresh(username);
    }

    // JWT Refresh 토큰 삭제 메소드
    @Transactional
    public void removeRefresh(String refreshToken) {
        refreshRepository.deleteByRefresh(refreshToken);
    }

    // 특정 유저 Refresh 토큰 모두 삭제 (탈퇴)
    @Transactional
    public void removeRefreshUser(String username) {
        refreshRepository.deleteByUsername(username);
    }
}
