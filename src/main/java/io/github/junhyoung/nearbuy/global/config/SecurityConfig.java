package io.github.junhyoung.nearbuy.global.config;

import io.github.junhyoung.nearbuy.global.filter.LoginFilter;
import io.github.junhyoung.nearbuy.global.handler.RefreshTokenLogoutHandler;
import io.github.junhyoung.nearbuy.jwt.service.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final AuthenticationSuccessHandler socialSuccessHandler;
    private final JwtService jwtService;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          @Qualifier("LoginSuccessHandler") AuthenticationSuccessHandler loginSuccessHandler,
                          @Qualifier("SocialSuccessHandler") AuthenticationSuccessHandler socialSuccessHandler,
                          JwtService jwtService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.loginSuccessHandler = loginSuccessHandler;
        this.socialSuccessHandler = socialSuccessHandler;
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
                        .successHandler(socialSuccessHandler))

                // 3. 로그아웃 handler
                .logout(logout -> logout
                        .addLogoutHandler(new RefreshTokenLogoutHandler(jwtService)))

                // 4. 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/user/join").permitAll() // 로그인, 회원가입 경로는 허용
                        .anyRequest().authenticated() // 나머지 경로는 인증 필요
                )

                // 5. 커스텀 필터 추가
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), loginSuccessHandler), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



}
