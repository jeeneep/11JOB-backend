package com.the11job.backend.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Querydsl 사용을 위한 JPAQueryFactory Bean 설정
 */
@Configuration
public class QuerydslConfig {

    // 현재 트랜잭션의 EntityManager 주입
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        // JPAQueryFactory는 EntityManager를 사용하여 쿼리를 생성하고 실행합니다.
        return new JPAQueryFactory(entityManager);
    }
}