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
// LoginFilter의 attemptAuthentication() -> LocalLoginService의 loadUserByUsername
// -> LoginFilter의 successfulAuthentication() -> LocalLoginSuccessHandler의 onAuthenticationSuccess() 순서로 진행
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

        // 3. 인증 토큰 생성 (아직 미인증된 인증 토큰임 - 검증되지 않은 출입 요청서)
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        setDetails(request, authRequest);

        // 4. AuthenticationManager에게 인증 위임 (보안관리자에게 미검증 출입 요청서 전달) ; 즉, LoginFilter는 인증 처리 안하고 위임하고 있음
        // ㄴ 매니저의 authenticate() 메서드를 호출하는데, 내부적으로 성공하면 인증된 토큰 반환 / 실패하면 예외를 반환해주는데 이 객체가 Authentication이다.
        return this.getAuthenticationManager().authenticate(authRequest);

        // 5. AuthenticationManager의 구현체는 AuthenticationProvider인데, 이건 코드상으로 직접 보여지지 않는다.
        // ㄴ AuthenticationProvider 중에서도 ID/PW 방식에 맞는 DaoAuthenticationProvider가 선택되며,
        //    해당 Provider는 사용자 정보를 가져오기 위해 UserDetailsService(본 프로젝트에서는 LocalLoginService)에 요청을 보낸다.
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


    // Provider가 LocalLoginService(UserDetailsService)로부터 비밀번호 검증을 통해 Authentication객체를 LoginFilter로 넘겨주는데,
    // 여기서 자동으로 successfulAuthentication() 메서드를 호출한다.
    // 내부적으로는 LocalLoginSuccessHandler의 onAuthenticationSuccess() 메서드를 호출 중이다. (SecurityConfig에서 LoginFilter 생성 시, 의존관계로 LocalLoginSuccessHandler 넣어줬음)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authResult);
    }
}