package io.github.junhyoung.nearbuy.auth.token.provider;

import io.github.junhyoung.nearbuy.auth.web.util.JWTUtil;
import io.github.junhyoung.nearbuy.auth.token.dto.JwtTokenDto;
import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtService jwtService;

    // Access/Refresh 토큰을 모두 발급하고 Refresh 토큰을 저장한다
    public JwtTokenDto issueTokens(Long id, String username, String role) {
        String accessToken = JWTUtil.createJWT(id, username, role, true);
        String refreshToken = createAndSaveRefreshToken(id, username, role);
        return new JwtTokenDto(accessToken, refreshToken);
    }

    // Refresh 토큰만 발급하고 저장한다
    public String issueRefreshToken(Long id, String username, String role) {

        return createAndSaveRefreshToken(id, username, role);
    }

    // Refresh 토큰 생성 및 저장 로직 (중복 제거)
    private String createAndSaveRefreshToken(Long id, String username, String role) {
        String refreshToken = JWTUtil.createJWT(id, username, role, false);
        jwtService.addRefresh(username, refreshToken);
        return refreshToken;
    }


}