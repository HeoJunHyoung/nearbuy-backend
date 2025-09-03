package io.github.junhyoung.nearbuy.post.repository;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long>, PostRepositoryCustom {

    @Query(value = "SELECT p FROM PostEntity p JOIN FETCH p.userEntity u")
    Slice<PostEntity> findAllWithUser(Pageable pageable);

    @Query(value = "SELECT p FROM PostEntity p " +
            "LEFT JOIN FETCH p.userEntity u " +
            "WHERE p.userEntity.id=:userId")
    Slice<PostEntity> findMyPosts(@Param(value="userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM PostEntity p " +
            "JOIN FETCH p.userEntity " +
            "LEFT JOIN FETCH p.postImageEntityList " +
            "WHERE p.id = :postId")
    Optional<PostEntity> findPostWithDetailsById(@Param("postId") Long postId);

}
