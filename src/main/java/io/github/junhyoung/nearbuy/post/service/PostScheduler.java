package io.github.junhyoung.nearbuy.post.service;

import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;
    private static final String VIEW_COUNT_KEY = "post:view_scores";

    // 5분마다 실행 (cron = "0 */5 * * * *")
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void syncViewCountsToDb() {
        log.info("조회수 동기화 스케줄러 시작");
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        // Redis에 있는 모든 조회수 정보를 가져옴
        Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOps.rangeWithScores(VIEW_COUNT_KEY, 0, -1);

        if (typedTuples == null || typedTuples.isEmpty()) {
            log.info("업데이트할 조회수 정보가 없습니다.");
            return;
        }

        // DB 업데이트
        typedTuples.forEach(tuple -> {
            Long postId = Long.valueOf(tuple.getValue());
            Long viewCount = tuple.getScore().longValue();

            postRepository.findById(postId).ifPresent(post -> {
                post.updateViewCount(viewCount);
            });
        });

        log.info("{}개의 게시물 조회수를 DB에 동기화했습니다.", typedTuples.size());
    }
}