package io.github.junhyoung.nearbuy.auth.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequestDto {
    private String refreshToken;
}