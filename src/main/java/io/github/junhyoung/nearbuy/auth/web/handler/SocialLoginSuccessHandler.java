package io.github.junhyoung.nearbuy.auth.web.handler;

import io.github.junhyoung.nearbuy.auth.token.provider.JwtProvider;
import io.github.junhyoung.nearbuy.auth.web.dto.CustomOAuth2User;
import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
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

        // Principal을 CustomOAuth2User 타입으로 캐스팅합니다.
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // CustomOAuth2User에서 UserPrincipal을 가져옵니다.
        UserPrincipal userPrincipal = oAuth2User.getUserPrincipal();

        Long id = userPrincipal.id();
        String username = userPrincipal.username();
        String role = userPrincipal.getRole();

        // Refresh 토큰 발급 및 저장 책임을 JwtProvider에 위임
        String refreshToken = jwtProvider.issueRefreshToken(id, username, "ROLE_" + role);

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