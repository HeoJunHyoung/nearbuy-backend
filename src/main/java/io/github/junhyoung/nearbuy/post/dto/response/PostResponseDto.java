package io.github.junhyoung.nearbuy.post.dto.response;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostResponseDto {

    private Long postId;

    private String authorNickname;

    private String title;

    private Integer price;

    private ProductCategory productCategory;

    private LocalDateTime createdAt;

    private PostImageResponseDto postImage;

    private PostResponseDto(PostEntity postEntity) {
        this.postId = postEntity.getId();
        this.authorNickname = postEntity.getUserEntity().getNickname();
        this.title = postEntity.getTitle();
        this.price = postEntity.getPrice();
        this.productCategory = postEntity.getProductCategory();
        this.createdAt = postEntity.getCreatedAt();
        this.postImage = postEntity.getPostImageEntityList().stream()
                .findFirst()
                .map(PostImageResponseDto::new)
                .orElse(null);
    }

    public static PostResponseDto from(PostEntity postEntity) {
        return new PostResponseDto(postEntity);
    }


}
