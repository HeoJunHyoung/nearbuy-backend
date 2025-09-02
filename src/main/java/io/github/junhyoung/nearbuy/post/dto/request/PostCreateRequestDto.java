package io.github.junhyoung.nearbuy.post.dto.request;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.entity.enumerate.PostStatus;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostCreateRequestDto {

    private String title;

    private String contents;

    private Integer price;

    private ProductCategory productCategory;

    public PostEntity toEntity(UserEntity userEntity) {
        return PostEntity.builder()
                .userEntity(userEntity)
                .title(this.title)
                .contents(this.contents)
                .price(this.price)
                .productCategory(this.productCategory)
                .postStatus(PostStatus.ON_SALE)
                .build();
    }

}
