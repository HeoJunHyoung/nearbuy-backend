package io.github.junhyoung.nearbuy.post.dto.response;

import io.github.junhyoung.nearbuy.post.entity.PostImageEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImageResponseDto {
    private Long id;
    private String imageUrl;

    public PostImageResponseDto(PostImageEntity postImageEntity) {
        this.id = postImageEntity.getId();
        this.imageUrl = postImageEntity.getImageUrl();
    }
}