package io.github.junhyoung.nearbuy.post.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.global.common.ApiResponse;
import io.github.junhyoung.nearbuy.post.dto.request.PostCreateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostDetailResponseDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPostApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestBody PostCreateRequestDto dto) {
        postService.createPost(userPrincipal.id(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> readPostApi() {
        List<PostResponseDto> postResponseDtos = postService.readPosts();
        return ResponseEntity.ok(ApiResponse.success(postResponseDtos));
    }

    @GetMapping("{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> readPostDetailApi(@PathVariable Long postId) {
        PostDetailResponseDto postDetailResponseDto = postService.readPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(postDetailResponseDto));
    }

    @PatchMapping("{postId}")
    public ResponseEntity<ApiResponse<Void>> updatePostApi(@PathVariable Long postId,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @RequestBody PostUpdateRequestDto dto) {
        postService.updatePost(postId, userPrincipal.id(), dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePostApi(@PathVariable Long postId,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deletePost(postId, userPrincipal.id());
        return ResponseEntity.ok(ApiResponse.success());
    }

}
