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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImageEntity> postImageEntityList = new ArrayList<>();

    private Long viewCount;

    @Builder
    public PostEntity(UserEntity userEntity, String title, String contents, Integer price, ProductCategory productCategory, PostStatus postStatus) {
        this.userEntity = userEntity;
        this.title = title;
        this.contents = contents;
        this.price = price;
        this.productCategory = productCategory;
        this.postStatus = postStatus;
        this.viewCount = 0L;
    }

    //== 연관관계 편의 메서드 ==//
    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;   // 게시글 작성자 설정
        userEntity.getPostEntityList().add(this);   // 사용자가 작성한 게시글에 현재 게시글 추가
    }

    public void addPostImageEntity(PostImageEntity postImageEntity) {
        this.postImageEntityList.add(postImageEntity);
        postImageEntity.setPost(this);
    }

    //== 내부 메서드 ==//
    public void updatePostDetail(PostUpdateRequestDto dto) {
        Optional.ofNullable(dto.getTitle()).ifPresent(newTitle -> this.title = newTitle);
        Optional.ofNullable(dto.getContents()).ifPresent(newContents -> this.contents = newContents);
        Optional.ofNullable(dto.getPostStatus()).ifPresent(newStatus -> this.postStatus = newStatus);
        Optional.ofNullable(dto.getPrice()).ifPresent(newPrice -> this.price = newPrice);
        Optional.ofNullable(dto.getProductCategory()).ifPresent(newCategory -> this.productCategory = newCategory);
    }

    public void updateViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

}
