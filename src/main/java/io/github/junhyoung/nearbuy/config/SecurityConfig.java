package io.github.junhyoung.nearbuy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.junhyoung.nearbuy.auth.web.filter.JWTFilter;
import io.github.junhyoung.nearbuy.auth.web.filter.LoginFilter;
import io.github.junhyoung.nearbuy.auth.web.handler.RefreshTokenLogoutHandler;
import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.service.SocialLoginService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationSuccessHandler localLoginSuccessHandler;
    private final AuthenticationSuccessHandler socialLoginSuccessHandler;
    private final SocialLoginService socialLoginService;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          @Qualifier("LocalLoginSuccessHandler") AuthenticationSuccessHandler localLoginSuccessHandler,
                          @Qualifier("SocialLoginSuccessHandler") AuthenticationSuccessHandler socialLoginSuccessHandler,
                          SocialLoginService socialLoginService,
                          ObjectMapper objectMapper,
                          JwtService jwtService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.localLoginSuccessHandler = localLoginSuccessHandler;
        this.socialLoginSuccessHandler = socialLoginSuccessHandler;
        this.socialLoginService = socialLoginService;
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
    }

    // 비밀번호 암호화 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 커스텀 자체 로그인 필터를 위한 AuthenticationManager Bean 수동 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // CORS Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 권한 계층
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withRolePrefix("ROLE_")
                .role(UserRoleType.ADMIN.name()).implies(UserRoleType.USER.name())
                .build();
    }

    // SecurityFilterChain 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 기본 설정
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 2. OAuth2 로그인 핸들러
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(socialLoginService)) // custom service를 명시적으로 지정
                        .successHandler(socialLoginSuccessHandler))

                // 3. 로그아웃 handler
                .logout(logout -> logout
                        .addLogoutHandler(new RefreshTokenLogoutHandler(jwtService, objectMapper)))

                // 4. 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll() // 로그인
                        .requestMatchers("/jwt/exchange", "/jwt/refresh").permitAll() // JWT 관련 API 호출
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/post", "/post/**").permitAll() // 게시글 조회는 모두 허용
                        .requestMatchers(HttpMethod.POST, "/user/exist", "/user/join").permitAll() // 회원가입
                        .requestMatchers(HttpMethod.GET, "/user").hasRole(UserRoleType.USER.name()) // 유저 정보 조회
                        .requestMatchers(HttpMethod.PUT, "/user").hasRole(UserRoleType.USER.name()) // 유저 정보 수정
                        .requestMatchers(HttpMethod.DELETE, "/user").hasRole(UserRoleType.USER.name()) // 유저 삭제
                        .anyRequest().authenticated()
                )

                // 5. CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 6. 커스텀 필터 추가
                .addFilterBefore(new JWTFilter(), LogoutFilter.class)
                .addFilterBefore(new LoginFilter(authenticationManager(authenticationConfiguration), localLoginSuccessHandler), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}
