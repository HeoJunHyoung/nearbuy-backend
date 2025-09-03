package io.github.junhyoung.nearbuy.post.dto.request;

import io.github.junhyoung.nearbuy.post.entity.enumerate.PostStatus;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchCond {

    private String title;

    private PostStatus postStatus;

    private ProductCategory productCategory;

    private Integer minPrice;

    private Integer maxPrice;

}
