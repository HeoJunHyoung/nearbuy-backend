package io.github.junhyoung.nearbuy.post.entity;

import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "favorite")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;

    @Builder
    public FavoriteEntity(UserEntity userEntity, PostEntity postEntity) {
        this.userEntity = userEntity;
        this.postEntity = postEntity;
    }
}
