package io.github.junhyoung.nearbuy.auth.token.controller;

import io.github.junhyoung.nearbuy.auth.token.dto.request.RefreshRequestDto;
import io.github.junhyoung.nearbuy.auth.token.dto.response.JWTResponseDto;
import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 5단계: 최종 토큰 교환 (JwtController)
// ㄴ 리다이렉트된 프론트엔드 페이지는 자바스크립트를 통해 쿠키에 담긴 Refresh Token을 확인합니다.
// ㄴ 이 Refresh Token을 가지고 백엔드의 POST /jwt/exchange API를 호출합니다.
// ㄴ auth.token.controller.JwtController는 이 요청을 받아 JwtService를 통해 Refresh Token을 검증하고,
//    마침내 클라이언트가 사용할 Access Token과 새로운 Refresh Token 한 쌍을 JSON 형태로 응답해줍니다.
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
