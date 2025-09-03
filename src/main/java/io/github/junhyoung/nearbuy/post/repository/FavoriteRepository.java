package io.github.junhyoung.nearbuy.post.repository;

import io.github.junhyoung.nearbuy.post.entity.FavoriteEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    Optional<FavoriteEntity> findByUserEntity_IdAndPostEntity_Id(Long userId, Long postId);

    @Query(value = "SELECT f FROM FavoriteEntity f " +
            "JOIN FETCH f.postEntity p " +
            "JOIN FETCH p.userEntity u " +
            "WHERE f.userEntity.id = :userId " +
            "ORDER BY p.createdAt DESC")
    Slice<FavoriteEntity> findFavoritesByUserId(@Param(value = "userId") Long userId, Pageable pageable);

}
