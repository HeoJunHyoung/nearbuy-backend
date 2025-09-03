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
import io.github.junhyoung.nearbuy.post.repository.FavoriteRepository;
import io.github.junhyoung.nearbuy.post.repository.PostImageRepository;
import io.github.junhyoung.nearbuy.post.repository.PostRepository;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
    private final FavoriteRepository favoriteRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VIEW_COUNT_KEY = "post:view_scores";

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
        Sort.Order viewCountOrder = pageable.getSort().getOrderFor("viewCount");

        if (viewCountOrder != null && viewCountOrder.isDescending()) {
            return searchPostsByViewCount(pageable);
        } else {
            Slice<PostEntity> posts = postRepository.search(cond, pageable);
            return posts.map(this::mapToPostResponseDtoWithViewCount);
        }
    }

    // 나의 게시글 전체 조회
    public Slice<MyPostResponseDto> readMyPosts(Long userId,Pageable pageable) {
        Slice<PostEntity> posts = postRepository.findMyPosts(userId, pageable);
        return posts.map(MyPostResponseDto::from);
    }

    // 게시글 세부 조회
    public PostDetailResponseDto readPostDetail(Long postId, Long userId) {
        // 1. 조회수 증가
        incrementViewCount(postId);

        // 2. 게시글 정보 조회
        PostEntity postEntity = postRepository.findPostWithDetailsById(postId)
                .orElseThrow(PostNotFoundException::new);

        boolean isFavorited = false;
        if (userId != null) {
            isFavorited = favoriteRepository.findByUserEntity_IdAndPostEntity_Id(userId, postId).isPresent();
        }

        // 3. 실시간 조회수 가져오기
        Double viewCount = redisTemplate.opsForZSet().score(VIEW_COUNT_KEY, String.valueOf(postId));

        return PostDetailResponseDto.from(postEntity, isFavorited, viewCount != null ? viewCount.longValue() : postEntity.getViewCount());
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



    //== 내부 헬퍼 메서드 ==//

    // [신규] 조회수 증가 메서드
    private void incrementViewCount(Long postId) {
        redisTemplate.opsForZSet().incrementScore(VIEW_COUNT_KEY, String.valueOf(postId), 1);
    }

    // 조회순 정렬을 위한 메서드
    private Slice<PostResponseDto> searchPostsByViewCount(Pageable pageable) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        long start = pageable.getOffset();
        // hasNext를 확인하기 위해 1개 더 요청
        long end = start + pageable.getPageSize();

        Set<String> postIdsStr = zSetOps.reverseRange(VIEW_COUNT_KEY, start, end);

        if ((postIdsStr == null) || postIdsStr.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        boolean hasNext = postIdsStr.size() > pageable.getPageSize();
        List<Long> postIds = postIdsStr.stream()
                .limit(pageable.getPageSize()) // 실제 페이지 사이즈만큼 자르기
                .map(Long::valueOf)
                .collect(Collectors.toList());

        List<PostEntity> postsFromDb = postRepository.findAllByIdIn(postIds);

        // Redis의 조회수 순서대로 정렬
        List<PostEntity> sortedPosts = postIds.stream()
                .flatMap(id -> postsFromDb.stream().filter(p -> p.getId().equals(id)))
                .collect(Collectors.toList());

        List<PostResponseDto> dtos = sortedPosts.stream()
                .map(this::mapToPostResponseDtoWithViewCount)
                .collect(Collectors.toList());

        return new SliceImpl<>(dtos, pageable, hasNext);
    }

    // DTO 변환 시 조회수 포함
    private PostResponseDto mapToPostResponseDtoWithViewCount(PostEntity postEntity) {
        Double viewCount = redisTemplate.opsForZSet().score(VIEW_COUNT_KEY, String.valueOf(postEntity.getId()));
        return PostResponseDto.from(postEntity, viewCount != null ? viewCount.longValue() : postEntity.getViewCount());
    }

}
