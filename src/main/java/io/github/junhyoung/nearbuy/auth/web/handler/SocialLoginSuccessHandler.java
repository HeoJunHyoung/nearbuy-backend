package io.github.junhyoung.nearbuy.auth.web.handler;

import io.github.junhyoung.nearbuy.auth.token.provider.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import jakarta.servlet.http.Cookie;
@Component
@Qualifier("SocialLoginSuccessHandler")
@RequiredArgsConstructor
public class SocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // Refresh 토큰 발급 및 저장 책임을 JwtProvider에 위임
        String refreshToken = jwtProvider.issueRefreshToken(username, "ROLE_" + role);

        // 응답 처리
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(10);

        response.addCookie(refreshCookie);
        response.sendRedirect("http://localhost:5173/cookie");
    }
}