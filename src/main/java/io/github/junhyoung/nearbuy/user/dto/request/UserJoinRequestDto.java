package io.github.junhyoung.nearbuy.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequestDto {

    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문과 숫자로 포함해서 4~20자리 이내로 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$", message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~16자리여야 합니다.")
    private String originPassword;

    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmPassword;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    private String nickname;

}