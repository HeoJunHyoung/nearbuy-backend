package io.github.junhyoung.nearbuy.post.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.junhyoung.nearbuy.post.dto.request.PostSearchCond;
import io.github.junhyoung.nearbuy.post.entity.PostEntity;
import io.github.junhyoung.nearbuy.post.entity.QPostEntity;
import io.github.junhyoung.nearbuy.post.entity.enumerate.PostStatus;
import io.github.junhyoung.nearbuy.post.entity.enumerate.ProductCategory;
import io.github.junhyoung.nearbuy.user.entity.QUserEntity;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import java.util.List;

import static io.github.junhyoung.nearbuy.post.entity.QPostEntity.*;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<PostEntity> search(PostSearchCond cond, Pageable pageable) {
        List<PostEntity> content = queryFactory
                .selectFrom(postEntity)
                .join(postEntity.userEntity, QUserEntity.userEntity).fetchJoin()
                .where(
                        titleContains(cond.getTitle()),
                        statusEq(cond.getPostStatus()),
                        categoryEq(cond.getProductCategory()),
                        priceBetween(cond.getMinPrice(), cond.getMaxPrice())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .orderBy(postEntity.createdAt.desc())
                .fetch();

        boolean hasNext = false;

        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? postEntity.title.contains(title) : null;
    }

    private BooleanExpression statusEq(PostStatus postStatus) {
        return postStatus != null ? postEntity.postStatus.eq(postStatus) : null;
    }

    private BooleanExpression categoryEq(ProductCategory productCategory) {
        return productCategory != null ? postEntity.productCategory.eq(productCategory) : null;
    }

    private BooleanExpression priceBetween(Integer minPrice, Integer maxPrice) {
        if (minPrice == null && maxPrice == null) {
            return null;
        }
        if (minPrice != null && maxPrice != null) {
            return postEntity.price.between(minPrice, maxPrice);
        }
        if (minPrice != null) {
            return postEntity.price.goe(minPrice); // Greater than or Equal to (>=)
        }
        return postEntity.price.loe(maxPrice); // Less than or Equal to (<=)
    }

}
