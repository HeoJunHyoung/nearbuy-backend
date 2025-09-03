package io.github.junhyoung.nearbuy.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory를 Spring Bean으로 등록하여 프로젝트 전역에서 주입받아 사용할 수 있도록 설정
     * EntityManager를 통해 JPA의 영속성 컨텍스트를 관리하며, 이를 기반으로 QueryDSL 쿼리를 생성
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
