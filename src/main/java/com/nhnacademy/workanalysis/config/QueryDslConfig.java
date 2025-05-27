package com.nhnacademy.workanalysis.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 사용을 위한 JPAQueryFactory 빈 설정 클래스입니다.
 */
@Configuration
public class QueryDslConfig {

    /**
     * JPAQueryFactory 빈 생성 메서드
     *
     * @param entityManager JPA EntityManager
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
