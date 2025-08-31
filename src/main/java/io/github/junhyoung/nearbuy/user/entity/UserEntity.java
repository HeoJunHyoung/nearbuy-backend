package io.github.junhyoung.nearbuy.user.entity;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Getter
public class UserEntity extends BaseEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToMany(mappedBy = "userEntity", fetch = FetchType.LAZY)
    private List<PostEntity> postEntityList = new ArrayList<>();

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
    public UserEntity(String username, String password, Boolean isLock, Boolean isSocial,
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
    public void updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        this.email = userUpdateRequestDto.getEmail();
        this.nickname = userUpdateRequestDto.getNickname();
    }

    public void updateUserPassword(String newPassword) {
        this.password = newPassword;
    }
}
