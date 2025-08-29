package io.github.junhyoung.nearbuy.jwt.repository;

import io.github.junhyoung.nearbuy.jwt.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refreshToken);

    @Transactional
    void deleteByRefresh(String refresh);

    @Transactional
    void deleteByUsername(String username);

    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime createdAt);

}
