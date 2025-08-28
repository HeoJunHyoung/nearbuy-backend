package io.github.junhyoung.nearbuy.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinRequestDto {

    String username;

    String originPassword;

    String confirmPassword;

    String email;

    String nickname;

}