package io.github.junhyoung.nearbuy.jwt.repository;

import io.github.junhyoung.nearbuy.jwt.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    Boolean existsByRefresh(String refreshToken);

    void deleteByRefresh(String refresh);

    void deleteByUsername(String username);

}
