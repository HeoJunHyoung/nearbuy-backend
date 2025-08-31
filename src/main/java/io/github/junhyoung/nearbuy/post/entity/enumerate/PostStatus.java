package io.github.junhyoung.nearbuy.post.entity.enumerate;

import lombok.Getter;

@Getter
public enum PostStatus {
    ON_SALE("판매중"),
    RESERVED("예약중"),
    SOLD_OUT("판매완료");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
