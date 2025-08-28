package io.github.junhyoung.nearbuy.jwt.entity;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "jwt_refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "refresh", nullable = false, length = 512)
    private String refresh;

    @Builder
    public RefreshToken(String username, String refresh) {
        this.username = username;
        this.refresh = refresh;
    }

}
