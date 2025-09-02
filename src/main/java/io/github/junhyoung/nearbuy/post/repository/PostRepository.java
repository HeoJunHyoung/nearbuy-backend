package io.github.junhyoung.nearbuy.post.repository;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    @Transactional
    void deleteByImageUrlIn(List<String> imageUrls);

}
