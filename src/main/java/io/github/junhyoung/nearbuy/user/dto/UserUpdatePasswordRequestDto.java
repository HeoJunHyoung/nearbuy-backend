package io.github.junhyoung.nearbuy.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdatePasswordRequestDto {

    @NotBlank(message = "기존 비밀번호를 입력해주세요.")
    private String originPassword;

    @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~16자리여야 합니다.")
    private String newPassword;

    @NotBlank(message = "새로운 비밀번호 확인을 입력해주세요.")
    private String newConfirmPassword;

}
