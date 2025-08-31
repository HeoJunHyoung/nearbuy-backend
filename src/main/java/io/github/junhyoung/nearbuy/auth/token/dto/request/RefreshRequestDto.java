package io.github.junhyoung.nearbuy.auth.token.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequestDto {

    @NotBlank
    private String refreshToken;

}
