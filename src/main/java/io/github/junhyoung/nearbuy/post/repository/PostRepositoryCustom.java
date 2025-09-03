package io.github.junhyoung.nearbuy.post.repository;

import io.github.junhyoung.nearbuy.post.dto.request.PostSearchCond;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostRepositoryCustom {
    Slice<PostEntity> search(PostSearchCond cond, Pageable pageable);
}
