package io.github.junhyoung.nearbuy.post.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.global.common.ApiResponse;
import io.github.junhyoung.nearbuy.post.dto.request.PostCreateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.response.MyPostResponseDto;
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

    // 게시글 등록
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<Void>> createPostApi(
                        @AuthenticationPrincipal UserPrincipal userPrincipal,
                        @RequestPart("postCreateRequestDto") PostCreateRequestDto postCreateRequestDto,
                        @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {

        postService.createPost(userPrincipal.id(), postCreateRequestDto, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Slice<PostResponseDto>>> readPostApi(Pageable pageable) {
        Slice<PostResponseDto> postResponseDtos = postService.readPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(postResponseDtos));
    }

    // 내 게시글 전체 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Slice<MyPostResponseDto>>> readMyPostApi(
                                @AuthenticationPrincipal UserPrincipal userPrincipal,
                                Pageable pageable) {
        Slice<MyPostResponseDto> postResponseDtos = postService.readMyPosts(userPrincipal.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success(postResponseDtos));
    }

    // 게시글 상세 조회
    @GetMapping("{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> readPostDetailApi(@PathVariable Long postId) {
        PostDetailResponseDto postDetailResponseDto = postService.readPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(postDetailResponseDto));
    }

    // 게시글 수정
    @PatchMapping(value = "/{postId}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<Void>> updatePostApi(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestPart("dto") PostUpdateRequestDto dto,
            @RequestPart(value = "addImages", required = false) List<MultipartFile> addImages) throws IOException {

        postService.updatePost(postId, userPrincipal.id(), dto, addImages);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 게시글 삭제
    @DeleteMapping("{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePostApi(@PathVariable Long postId,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deletePost(postId, userPrincipal.id());
        return ResponseEntity.ok(ApiResponse.success());
    }

}
