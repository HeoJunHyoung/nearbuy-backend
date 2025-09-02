package io.github.junhyoung.nearbuy.user.dto.response;

import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponseDto {
    private Long userId;
    private String username;
    private Boolean social;
    private String nickname;
    private String email;

    private UserResponseDto(Long userId, String username, Boolean social, String nickname, String email) {
        this.userId = userId;
        this.username = username;
        this.social = social;
        this.nickname = nickname;
        this.email = email;
    }

    private UserResponseDto(UserEntity userEntity) {
        this.userId = userEntity.getId();
        this.username = userEntity.getUsername();
        this.social = userEntity.getIsSocial();
        this.nickname = userEntity.getNickname();
        this.email = userEntity.getEmail();
    }

    public static UserResponseDto from(UserEntity userEntity) {
        return new UserResponseDto(userEntity);
    }

}
