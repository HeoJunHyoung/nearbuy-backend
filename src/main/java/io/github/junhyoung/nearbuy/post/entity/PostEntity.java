package io.github.junhyoung.nearbuy.post.entity;

import io.github.junhyoung.nearbuy.global.entity.BaseEntity;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.entity.enumerate.PostStatus;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PostEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(name = "title")
    private String title;

    @Column(name = "contents")
    private String contents;

    @Column(name = "price")
    private Integer price;

    @Column(name = "post_status")
    @Enumerated(value = EnumType.STRING)
    private PostStatus postStatus;

    @Column(name = "product_category")
    @Enumerated(value = EnumType.STRING)
    private ProductCategory productCategory;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Builder
    public PostEntity(UserEntity userEntity, String title, String contents, Integer price, ProductCategory productCategory, PostStatus postStatus, String imageUrl) {
        this.userEntity = userEntity;
        this.title = title;
        this.contents = contents;
        this.price = price;
        this.productCategory = productCategory;
        this.postStatus = postStatus;
        this.imageUrl = imageUrl;
    }

    //== 연관관계 편의 메서드 ==//
    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;   // 게시글 작성자 설정
        userEntity.getPostEntityList().add(this);   // 사용자가 작성한 게시글에 현재 게시글 추가
    }

    //== 내부 메서드 ==//
    public void updatePostDetail(PostUpdateRequestDto dto) {
        if (dto.getTitle() != null) {
            this.title = dto.getTitle();
        }
        if (dto.getContents() != null) {
            this.contents = dto.getContents();
        }
        if (dto.getPostStatus() != null) {
            this.postStatus = dto.getPostStatus();
        }
        if (dto.getPrice() != null) {
            this.price = dto.getPrice();
        }
        if (dto.getProductCategory() != null) {
            this.productCategory = dto.getProductCategory();
        }
        if (dto.getImageUrl() != null) {
            this.imageUrl = dto.getImageUrl();
        }
    }

}
