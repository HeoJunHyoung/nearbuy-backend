package io.github.junhyoung.nearbuy.post.service;

import io.github.junhyoung.nearbuy.global.exception.business.PostNotFoundException;
import io.github.junhyoung.nearbuy.global.exception.business.UserNotFoundException;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.entity.FavoriteEntity;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.repository.FavoriteRepository;
import io.github.junhyoung.nearbuy.post.repository.PostRepository;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 게시글 즐겨찾기 추가/삭제 (토글)
    @Transactional
    public boolean toggleFavorite(Long userId, Long postId) {
        Optional<FavoriteEntity> favoriteOptional = favoriteRepository.findByUserEntity_IdAndPostEntity_Id(userId, postId);

        if (favoriteOptional.isPresent()) {
            // 이미 즐겨찾기 상태이면 삭제
            favoriteRepository.delete(favoriteOptional.get());
            return false;
        } else {
            // 즐겨찾기가 아니면 추가
            UserEntity user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
            PostEntity post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

            FavoriteEntity favorite = FavoriteEntity.builder()
                    .userEntity(user)
                    .postEntity(post)
                    .build();

            favoriteRepository.save(favorite);
            return true;
        }
    }


    // 특정 게시글의 즐겨찾기 상태 조회
    @Transactional(readOnly = true)
    public boolean checkFavoriteStatus(Long userId, Long postId) {
        return favoriteRepository.findByUserEntity_IdAndPostEntity_Id(userId, postId).isPresent();
    }

    // 나의 관심 목록 조회
    @Transactional(readOnly = true)
    public Slice<PostResponseDto> getMyFavoritePosts(Long userId, Pageable pageable) {
        Slice<FavoriteEntity> favorites = favoriteRepository.findFavoritesByUserId(userId, pageable);
        // Favorite 슬라이스를 PostResponseDto 슬라이스로 변환
        return favorites.map(favorite -> PostResponseDto.from(favorite.getPostEntity()));
    }


}
