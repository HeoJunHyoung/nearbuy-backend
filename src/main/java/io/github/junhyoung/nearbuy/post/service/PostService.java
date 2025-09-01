package io.github.junhyoung.nearbuy.post.service;

import io.github.junhyoung.nearbuy.post.dto.request.PostCreateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostDetailResponseDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.repository.PostRepository;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 생성
    @Transactional
    public void createPost(Long authorId, PostCreateRequestDto dto) {
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        PostEntity postEntity = dto.toEntity(author);
        postEntity.setUserEntity(author);

        postRepository.save(postEntity);
    }

    // 게시글 전체 조회
    public List<PostResponseDto> readPosts() {
        List<PostEntity> posts = postRepository.findAll();

        List<PostResponseDto> postResponseDtos = posts.stream()
                .map((post) -> PostResponseDto.builder()
                        .postEntity(post)
                        .build()).collect(Collectors.toList());

        return postResponseDtos;
    }

    // 게시글 세부 조회
    public PostDetailResponseDto readPostDetail(Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글입니다."));

        return PostDetailResponseDto.builder()
                .postEntity(postEntity)
                .build();
    }

    // 게시글 세부 정보 수정
    @Transactional
    public void updatePost(Long postId, Long userId, PostUpdateRequestDto dto) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글입니다."));
        if (!postEntity.getUserEntity().getId().equals(userId)) {
            throw new AccessDeniedException("게시글 수정은 작성자만 가능합니다.");
        }
        postEntity.updatePostDetail(dto);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 게시글입니다."));
        if (!postEntity.getUserEntity().getId().equals(userId)) {
            throw new AccessDeniedException("게시글 삭제는 작성자만 가능합니다.");
        }
        postRepository.delete(postEntity);
    }

}
