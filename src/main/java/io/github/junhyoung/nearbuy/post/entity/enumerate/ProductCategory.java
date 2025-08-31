package io.github.junhyoung.nearbuy.post.entity.enumerate;

import lombok.Getter;

@Getter
public enum ProductCategory {
    DIGITAL_DEVICE("디지털기기"),
    FURNITURE_INTERIOR("인테리어"),
    CLOTHING("의류"),
    HOME_APPLIANCES("가전"),
    BEAUTY("뷰티"),
    BOOKS("도서");

    private final String description;

    ProductCategory(String description) {
        this.description = description;
    }
}
