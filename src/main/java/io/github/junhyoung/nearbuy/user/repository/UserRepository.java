package io.github.junhyoung.nearbuy.user.repository;

import io.github.junhyoung.nearbuy.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
