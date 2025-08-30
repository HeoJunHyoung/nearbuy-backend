package io.github.junhyoung.nearbuy.auth.filter;

import io.github.junhyoung.nearbuy.auth.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JWTFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 토큰을 추출하는 로직을 별도 메서드로 분리
        String accessToken = resolveToken(request);

        // 2. 토큰이 유효하지 않으면 바로 에러 응답을 보내고 필터 체인을 종료
        if (!StringUtils.hasText(accessToken) || !JWTUtil.isValid(accessToken, true)) {
            // 토큰이 없거나 유효하지 않은 경우, 다음 필터로 계속 진행
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰이 유효한 경우, 인증 정보를 생성하고 SecurityContext에 등록
        setAuthentication(accessToken);

        filterChain.doFilter(request, response);
    }
    
    // 헤더에서 토큰 정보를 추출하는 책임을 담당하는 private 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    // 인증 정보를 SecurityContext에 설정하는 책임을 담당하는 private 메서드
    private void setAuthentication(String accessToken) {
        String username = JWTUtil.getUsername(accessToken);
        String role = JWTUtil.getRole(accessToken);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}