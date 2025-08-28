package io.github.junhyoung.nearbuy.user.domain;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.user.domain.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.domain.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.UserJoinRequestDto;
import io.github.junhyoung.nearbuy.user.dto.UserUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
@Getter
public class User extends BaseEntity implements UserDetails {

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.roleType.name()));
    }

    @Override
    public String getUsername() {
        return this.username; // `UserDetails`의 getUsername()은 PK가 아닌 로그인 ID를 반환해야 함
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true: 만료 안됨)
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.isLock; // 계정 잠김 여부 (true: 잠기지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부 (true: 만료 안됨)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (true: 활성화됨)
    }

    //== 내부 비즈니스 로직 ==//
    public void updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        this.email = userUpdateRequestDto.getEmail();
        this.nickname = userUpdateRequestDto.getNickname();
    }
}
