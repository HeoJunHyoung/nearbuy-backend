package io.github.junhyoung.nearbuy.auth.web.filter;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.auth.web.util.JWTUtil;
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

        // UserPrincipal 객체를 생성하여 principal로 사용합니다.
        UserPrincipal userPrincipal = new UserPrincipal(username, authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.authorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}