package io.github.junhyoung.nearbuy.auth.web.filter;

import io.github.junhyoung.nearbuy.auth.web.dto.LoginRequestDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

/**
 * Spring Security에서 기본적으로 제공하는 로그인 필터를 사용하지 않는 이유
 * ㄴ 기본 필터는 form-data 형태의 요청을 처리하도록 설계되어 있음
 * ㄴ 현재 프로젝트 구조 상 JSON 형태의 요청 본문을 처리하기 위해서는 커스텀 필터를 만들어서 ObjectMapper로 DTO에 파싱하는 과정이 추가적으로 필요함
 */
public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/login");

    public LoginFilter(AuthenticationManager authenticationManager, AuthenticationSuccessHandler authenticationSuccessHandler) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        // 1. DTO를 사용해 요청 파싱 로직을 위임
        LoginRequestDto loginRequest = parseLoginRequest(request);

        // 2. DTO에서 username과 password를 가져와 Null-safe하게 처리
        String username = (loginRequest.getUsername() != null) ? loginRequest.getUsername().trim() : "";
        String password = (loginRequest.getPassword() != null) ? loginRequest.getPassword() : "";

        // 3. 인증 토큰 생성
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        setDetails(request, authRequest);

        // 4. AuthenticationManager에게 인증 위임
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    // 파싱 메서드
    private LoginRequestDto parseLoginRequest(HttpServletRequest request) {
        try {
            return objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
        } catch (IOException e) {
            // 5. 예외 발생
            throw new AuthenticationServiceException("Failed to parse login request body", e);
        }
    }

    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
    }
}