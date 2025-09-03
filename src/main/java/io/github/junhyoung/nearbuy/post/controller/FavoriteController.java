package io.github.junhyoung.nearbuy.post.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.global.common.ApiResponse;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 즐겨찾기 추가/삭제 (토글)
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> toggleFavorite(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isFavorited = favoriteService.toggleFavorite(userPrincipal.id(), postId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("isFavorited", isFavorited)));
    }

    // 특정 게시글의 즐겨찾기 상태 확인
    @GetMapping("/{postId}/status")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavoriteStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        boolean isFavorited = favoriteService.checkFavoriteStatus(userPrincipal.id(), postId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("isFavorited", isFavorited)));
    }

    // 나의 관심 목록 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Slice<PostResponseDto>>> getMyFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {
        Slice<PostResponseDto> myFavoritePosts = favoriteService.getMyFavoritePosts(userPrincipal.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success(myFavoritePosts));
    }
}
