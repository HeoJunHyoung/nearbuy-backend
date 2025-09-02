package io.github.junhyoung.nearbuy.post.dto.response;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.entity.enumerate.PostStatus;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyPostResponseDto {

    private Long postId;

    private String title;

    private Integer price;

    private LocalDateTime createdAt;

    private PostImageResponseDto postImage;

    private PostStatus status;

    private MyPostResponseDto(PostEntity postEntity) {
        this.postId = postEntity.getId();
        this.title = postEntity.getTitle();
        this.price = postEntity.getPrice();
        this.createdAt = postEntity.getCreatedAt();
        this.status = postEntity.getPostStatus();
        this.postImage = postEntity.getPostImageEntityList().stream()
                .findFirst()
                .map(PostImageResponseDto::new)
                .orElse(null);
    }

    public static MyPostResponseDto from(PostEntity postEntity) {
        return new MyPostResponseDto(postEntity);
    }

}
