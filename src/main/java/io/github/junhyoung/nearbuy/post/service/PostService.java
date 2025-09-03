package io.github.junhyoung.nearbuy.post.service;

import io.github.junhyoung.nearbuy.global.exception.business.PostNotFoundException;
import io.github.junhyoung.nearbuy.global.exception.business.UserNotFoundException;
import io.github.junhyoung.nearbuy.global.util.FileStore;
import io.github.junhyoung.nearbuy.post.dto.request.PostCreateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.request.PostSearchCond;
import io.github.junhyoung.nearbuy.post.dto.request.PostUpdateRequestDto;
import io.github.junhyoung.nearbuy.post.dto.response.MyPostResponseDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostDetailResponseDto;
import io.github.junhyoung.nearbuy.post.dto.response.PostResponseDto;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.entity.PostImageEntity;
import io.github.junhyoung.nearbuy.post.repository.PostImageRepository;
import io.github.junhyoung.nearbuy.post.repository.PostRepository;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final FileStore fileStore;

    // 게시글 생성
    @Transactional
    public void createPost(Long authorId, PostCreateRequestDto dto, List<MultipartFile> images) throws IOException {
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(UserNotFoundException::new);

        // 1. PostEntity 먼저 생성 (Builder에서 imageUrl 제거)
        PostEntity postEntity = dto.toEntity(author);
        postEntity.setUserEntity(author);

        // 2. 파일들 저장
        List<String> imageUrls = fileStore.storeFiles(images);

        // 3. 각 이미지 URL을 PostImage 엔티티로 만들어 PostEntity에 추가
        for (String imageUrl : imageUrls) {
            PostImageEntity postImageEntity = PostImageEntity.builder()
                    .imageUrl(imageUrl)
                    .build();
            postEntity.addPostImageEntity(postImageEntity);
        }

        postRepository.save(postEntity);
    }

    // 게시글 전체 조회
    public Slice<PostResponseDto> readPosts(Pageable pageable) {
        Slice<PostEntity> posts = postRepository.findAllWithUser(pageable);
        return posts.map(PostResponseDto::from);
    }

    // 게시글 조건 검색
    public Slice<PostResponseDto> searchPosts(PostSearchCond cond, Pageable pageable) {
        Slice<PostEntity> posts = postRepository.search(cond, pageable);
        return posts.map(PostResponseDto::from);
    }

    // 나의 게시글 전체 조회
    public Slice<MyPostResponseDto> readMyPosts(Long userId,Pageable pageable) {
        Slice<PostEntity> posts = postRepository.findMyPosts(userId, pageable);
        return posts.map(MyPostResponseDto::from);
    }

    // 게시글 세부 조회
    public PostDetailResponseDto readPostDetail(Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        return PostDetailResponseDto.from(postEntity);
    }

    // 게시글 세부 정보 수정
    @Transactional
    public void updatePost(Long postId, Long userId, PostUpdateRequestDto dto, List<MultipartFile> addImages) throws IOException {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        // 2. 권한 확인
        if (!postEntity.getUserEntity().getId().equals(userId)) {
            throw new AccessDeniedException("게시글 수정은 작성자만 가능합니다.");
        }

        // 3. 텍스트 정보 업데이트 (엔티티 내부 메서드 호출)
        postEntity.updatePostDetail(dto);

        // 4. 이미지 삭제 처리
        if (dto.getDeleteImageUrls() != null && !dto.getDeleteImageUrls().isEmpty()) {
            postImageRepository.deleteByImageUrlIn(dto.getDeleteImageUrls());
        }

        // 5. 이미지 추가 처리
        List<String> newImageUrls = fileStore.storeFiles(addImages);
        for (String imageUrl : newImageUrls) {
            PostImageEntity newPostImage = PostImageEntity.builder()
                    .imageUrl(imageUrl)
                    .build();
            postEntity.addPostImageEntity(newPostImage);
        }
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);

        if (!postEntity.getUserEntity().getId().equals(userId)) {
            throw new AccessDeniedException("게시글 삭제는 작성자만 가능합니다.");
        }
        postRepository.delete(postEntity);
    }

}
