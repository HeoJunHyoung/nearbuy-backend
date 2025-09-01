package io.github.junhyoung.nearbuy.user.entity;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.global.exception.business.InvalidPasswordException;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.request.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @OneToMany(mappedBy = "userEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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
    public void updateUser(String nickname, String email) {
        this.email = email;
        this.nickname = nickname;
    }

    public void updateUserPassword(String originPassword, String newPassword, String newConfirmPassword, PasswordEncoder passwordEncoder) {
        // 1. 기존 비밀번호 검증
        if (!passwordEncoder.matches(originPassword, this.password)) {
            throw new InvalidPasswordException("기존 비밀번호가 일치하지 않습니다.");
        }
        // 2. 새 비밀번호 확인 일치 검증
        if (!newPassword.equals(newConfirmPassword)) {
            throw new InvalidPasswordException("새 비밀번호가 서로 일치하지 않습니다.");
        }
        // 3. 새 비밀번호로 변경
        this.password = passwordEncoder.encode(newPassword);
    }
}
