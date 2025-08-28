package io.github.junhyoung.nearbuy.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {

    String username;

    String password;

    String email;

    String nickname;

}