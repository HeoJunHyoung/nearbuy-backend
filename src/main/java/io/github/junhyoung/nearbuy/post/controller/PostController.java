package io.github.junhyoung.nearbuy.post.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.post.dto.request.PostCreateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostDeleteRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostDetailResponseDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<String> createPostApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                @RequestBody PostCreateRequestDto dto) {
        postService.createPost(userPrincipal.id(), dto);
        return ResponseEntity.status(201).body("게시글 등록이 완료되었습니다.");
    }

    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> readPostApi() {
        List<PostResponseDto> postResponseDtos = postService.readPosts();
        return ResponseEntity.status(200).body(postResponseDtos);
    }

    // 특정 게시글 상세 조회
    @GetMapping("{postId}")
    public ResponseEntity<PostDetailResponseDto> readPostDetailApi(@PathVariable Long postId) {
        PostDetailResponseDto postDetailResponseDto = postService.readPostDetail(postId);
        return ResponseEntity.status(200).body(postDetailResponseDto);
    }

    // 게시글 세부 정보 수정
    @PutMapping("{postId}")
    public ResponseEntity<String> updatePostApi(@PathVariable Long postId,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                @RequestBody PostUpdateRequestDto dto) {
        postService.updatePost(postId, userPrincipal.id(), dto);
        return ResponseEntity.status(200).body("게시글 수정이 완료되었습니다.");
    }


    // DELETE
    @DeleteMapping("{postId}")
    public ResponseEntity<String> deletePostApi(@PathVariable Long postId,
                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deletePost(postId, userPrincipal.id());
        return ResponseEntity.status(200).body("게시글 삭제가 완료되었습니다.");
    }

}
