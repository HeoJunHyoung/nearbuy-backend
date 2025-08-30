package io.github.junhyoung.nearbuy.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequestDto {
    private String refreshToken;
}