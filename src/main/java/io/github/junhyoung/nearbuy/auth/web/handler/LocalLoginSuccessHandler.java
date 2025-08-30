package io.github.junhyoung.nearbuy.auth.web.handler;

import io.github.junhyoung.nearbuy.auth.web.util.ResponseWriterUtil;
import io.github.junhyoung.nearbuy.auth.token.dto.JwtTokenDto;
import io.github.junhyoung.nearbuy.auth.token.provider.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
@Qualifier("LocalLoginSuccessHandler")
@RequiredArgsConstructor
public class LocalLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        // 토큰 발급 및 저장 책임을 JwtProvider에 위임
        JwtTokenDto tokens = jwtProvider.issueTokens(username, role);

        // 응답 작성 책임을 ResponseWriterUtil에 위임
        ResponseWriterUtil.writeJson(response, tokens);
    }
}