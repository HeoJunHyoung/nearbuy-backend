package io.github.junhyoung.nearbuy.post.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.global.common.ApiResponse;
import io.github.junhyoung.nearbuy.post.dto.request.PostCreateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostDetailResponseDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<Void>> createPostApi(
                        @AuthenticationPrincipal UserPrincipal userPrincipal,
                        @RequestPart("postCreateRequestDto") PostCreateRequestDto postCreateRequestDto,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        postService.createPost(userPrincipal.id(), postCreateRequestDto, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<PostResponseDto>>> readPostApi(Pageable pageable) {
        Slice<PostResponseDto> postResponseDtos = postService.readPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(postResponseDtos));
    }

    @GetMapping("{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> readPostDetailApi(@PathVariable Long postId) {
        PostDetailResponseDto postDetailResponseDto = postService.readPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(postDetailResponseDto));
    }

    @PatchMapping(value = "/{postId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<Void>> updatePostApi(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("dto") PostUpdateRequestDto dto,
            @RequestPart(value = "addImages", required = false) List<MultipartFile> addImages) throws IOException {

        postService.updatePost(postId, userPrincipal.id(), dto, addImages);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePostApi(@PathVariable Long postId,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deletePost(postId, userPrincipal.id());
        return ResponseEntity.ok(ApiResponse.success());
    }

}
