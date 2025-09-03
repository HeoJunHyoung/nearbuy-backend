package io.github.junhyoung.nearbuy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.junhyoung.nearbuy.user.entity.QUserEntity; // Q-Type 임포트
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional // 테스트 환경에서는 테스트 후 DB를 롤백시켜주는 어노테이션
class NearbuyApplicationTests {

    @Autowired
    EntityManager em;

    @Autowired
    UserRepository userRepository;

    // QuerydslConfig에 의해 Bean으로 등록된 JPAQueryFactory를 주입받습니다.
    @Autowired
    JPAQueryFactory queryFactory;

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("QueryDSL 설정 및 동작 테스트")
    void querydslTest() {
        // given: 테스트를 위한 데이터 준비
        UserEntity newUser = UserEntity.builder()
                .username("querydsl_user")
                .password("password123!")
                .email("test@test.com")
                .nickname("쿼리dsl테스터")
                .roleType(UserRoleType.USER)
                .isLock(false)
                .isSocial(false)
                .build();
        userRepository.save(newUser);

        // 영속성 컨텍스트 초기화 (DB에 물리적으로 반영 후 컨텍스트를 비워 순수한 조회를 위함)
        em.flush();
        em.clear();

        // when: QueryDSL을 사용하여 데이터 조회
        // QUserEntity는 build 시점에 자동으로 생성된 클래스입니다.
        QUserEntity qUserEntity = QUserEntity.userEntity;

        UserEntity foundUser = queryFactory
                .selectFrom(qUserEntity)
                .where(qUserEntity.username.eq("querydsl_user"))
                .fetchOne();

        // then: 결과 검증
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("querydsl_user");
        assertThat(foundUser.getId()).isEqualTo(newUser.getId());
    }
}