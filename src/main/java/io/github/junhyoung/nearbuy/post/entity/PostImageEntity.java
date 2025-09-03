package io.github.junhyoung.nearbuy.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PostImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    public PostImageEntity(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    //== 연관관계 편의 메서드 ==//
    public void setPost(PostEntity post) {
        this.post = post;
    }


}
