package io.github.junhyoung.nearbuy.user.repository;

import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByUsername(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.username=:username AND u.isLock=false AND u.isSocial=false")
    Optional<UserEntity> findByUsernameAndIsLockAndIsSocial(@Param("username") String username, Boolean isLock, Boolean isSocial);

    Optional<UserEntity> findByUsernameAndIsLock(String username, Boolean isLock);
    void deleteByUsername(String username);

}
