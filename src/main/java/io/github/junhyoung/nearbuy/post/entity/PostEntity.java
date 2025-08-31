package io.github.junhyoung.nearbuy.post.entity;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "price")
    private Integer price;

    @Column(name = "product_category")
    private ProductCategory productCategory;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Builder
    public PostEntity(UserEntity userEntity, String title, String content, Integer price, ProductCategory productCategory, String imageUrl) {
        this.userEntity = userEntity;
        this.title = title;
        this.content = content;
        this.price = price;
        this.productCategory = productCategory;
        this.imageUrl = imageUrl;
    }

}
