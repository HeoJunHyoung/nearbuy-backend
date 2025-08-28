package io.github.junhyoung.nearbuy.user.domain;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.user.domain.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.domain.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.UserRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Getter
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", unique = true, nullable = false, updatable = false)
    private String username;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="is_lock", nullable = false)
    private Boolean isLock;

    @Column(name="is_social", nullable = false)
    private Boolean isSocial;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "social_provider_type")
    private SocialProviderType socialProviderType;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role_type")
    private UserRoleType roleType;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Builder
    public User(String username, String password, Boolean isLock, Boolean isSocial,
                SocialProviderType socialProviderType, UserRoleType roleType,
                String nickname, String email) {
        this.username = username;
        this.password = password;
        this.isLock = isLock;
        this.isSocial = isSocial;
        this.socialProviderType = socialProviderType;
        this.roleType = roleType;
        this.nickname = nickname;
        this.email = email;
    }

    //== 내부 비즈니스 로직 ==//
    public void updateUser(UserRequestDto userRequestDto) {
        this.email = userRequestDto.getEmail();
        this.nickname = userRequestDto.getNickname();
    }

}
