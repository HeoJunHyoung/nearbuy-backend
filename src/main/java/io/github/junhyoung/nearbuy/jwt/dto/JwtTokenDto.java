package io.github.junhyoung.nearbuy.jwt.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class JwtTokenDto {
    private String accessToken;
    private String refreshToken;

    public JwtTokenDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}