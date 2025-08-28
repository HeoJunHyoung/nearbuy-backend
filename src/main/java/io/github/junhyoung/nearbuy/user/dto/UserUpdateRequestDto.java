package io.github.junhyoung.nearbuy.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    String username;

    String email;

    String nickname;

}
