package io.github.junhyoung.nearbuy.post.repository;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
}
