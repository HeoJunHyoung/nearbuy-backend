package io.github.junhyoung.nearbuy.post.dto.response;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.entity.enumerate.PostStatus;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostDetailResponseDto {


    private Long authorId;

    private Long postId;

    private String authorNickname;

    private String title;

    private String contents;

    private Integer price;

    private ProductCategory productCategory;

    private LocalDateTime createdAt;

    private PostStatus status;

    private List<PostImageResponseDto> postImages = new ArrayList<>();

    private PostDetailResponseDto(PostEntity postEntity) {
        this.postId = postEntity.getId();
        this.authorId = postEntity.getUserEntity().getId();
        this.authorNickname = postEntity.getUserEntity().getNickname();
        this.title = postEntity.getTitle();
        this.contents = postEntity.getContents();
        this.price = postEntity.getPrice();
        this.productCategory = postEntity.getProductCategory();
        this.createdAt = postEntity.getCreatedAt();
        this.status = postEntity.getPostStatus();
        this.postImages = postEntity.getPostImageEntityList().stream()
                .map(PostImageResponseDto::new)
                .collect(Collectors.toList());
    }

    public static PostDetailResponseDto from(PostEntity postEntity) {
        return new PostDetailResponseDto(postEntity);
    }

}
