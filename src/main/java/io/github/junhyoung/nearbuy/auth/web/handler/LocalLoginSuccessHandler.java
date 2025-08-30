package io.github.junhyoung.nearbuy.auth.web.handler;

import io.github.junhyoung.nearbuy.auth.web.util.ResponseWriterUtil;
import io.github.junhyoung.nearbuy.auth.token.dto.JwtTokenDto;
import io.github.junhyoung.nearbuy.auth.token.provider.JwtProvider;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
@Qualifier("LocalLoginSuccessHandler")
@RequiredArgsConstructor
public class LocalLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String username = authentication.getName();
        UserEntity user = userRepository.findByUsernameAndIsLock(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        JwtTokenDto tokens = jwtProvider.issueTokens(user.getId(), user.getUsername(), user.getRoleType().name());

        ResponseWriterUtil.writeJson(response, tokens);
    }
}