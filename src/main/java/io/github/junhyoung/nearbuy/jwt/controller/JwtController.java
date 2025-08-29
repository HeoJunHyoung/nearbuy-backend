package io.github.junhyoung.nearbuy.jwt.controller;

import io.github.junhyoung.nearbuy.jwt.dto.request.RefreshRequestDto;
import io.github.junhyoung.nearbuy.jwt.dto.response.JWTResponseDto;
import io.github.junhyoung.nearbuy.jwt.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/jwt")
public class JwtController {

    private final JwtService jwtService;

    // 소셜 로그인 쿠키 방식의 Refresh 토큰 헤더 방식으로 교환
    @PostMapping("/exchange")
    public JWTResponseDto jwtExchangeApi(HttpServletRequest request, HttpServletResponse response) {
        return jwtService.cookie2Header(request, response);
    }

    // Refresh 토큰으로 Access 토큰 재발급 (Rotate 포함)
    @PostMapping("/refresh")
    public JWTResponseDto jwtRefreshApi(@Validated @RequestBody RefreshRequestDto refreshRequestDto) {
        return jwtService.refreshRotate(refreshRequestDto);
    }

}
