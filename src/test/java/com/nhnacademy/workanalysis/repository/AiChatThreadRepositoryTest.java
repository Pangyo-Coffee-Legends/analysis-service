package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link AiChatThreadRepository}와 QueryDSL 기반의 커스텀 리포지토리에 대한 통합 테스트 클래스입니다.
 *
 * <p>
 * 이 클래스는 다음의 테스트를 수행합니다:
 * <ul>
 *   <li>사원 번호(mbNo)를 기준으로 쓰레드를 생성일(createdAt) 기준 내림차순 정렬하여 조회</li>
 *   <li>QueryDSL 환경이 필요한 커스텀 리포지토리를 위한 JPAQueryFactory 설정을 포함</li>
 * </ul>
 *
 * <p>
 * {@code @DataJpaTest}는 JPA 관련 컴포넌트만 로드하는 슬라이스 테스트 환경을 제공합니다.
 * 다만, {@link com.querydsl.jpa.impl.JPAQueryFactory}는 자동으로 Bean으로 등록되지 않으므로,
 * {@link TestConfiguration}을 통해 명시적으로 설정합니다.
 */
@DataJpaTest
@Import(AiChatThreadRepositoryTest.QueryDslTestConfig.class)
@TestPropertySource(properties = {
        "member.service.url=http://localhost:8080",
        "work.entry.service.url=http://localhost:8081"
})
// ✅ QueryDSL 설정 수동 주입
class AiChatThreadRepositoryTest {

    @Autowired
    private AiChatThreadRepository threadRepository;

    /**
     * 사원 번호(mbNo)를 기준으로 해당 사원이 생성한 모든 쓰레드를 조회하며,
     * 쓰레드 생성일(createdAt) 기준으로 내림차순 정렬이 정상적으로 동작하는지 검증합니다.
     *
     * 예시:
     * - "2번" → 최근 생성된 쓰레드
     * - "1번" → 먼저 생성된 쓰레드
     *
     * 기대 결과: ["2번", "1번"] 순서로 반환
     */
    @Test
    @DisplayName("mbNo 기준으로 쓰레드를 createdAt 기준 내림차순 정렬하여 조회")
    void findByMbNoOrderByCreatedAtDesc_success() {
        AiChatThread t1 = AiChatThread.create(1L, "1번");
        AiChatThread t2 = AiChatThread.create(1L, "2번");

        threadRepository.saveAll(List.of(t1, t2));

        List<AiChatThread> result = threadRepository.findByMbNoOrderByCreatedAtDesc(1L);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getTitle()).isEqualTo("2번"); // 최근 생성된 쓰레드가 먼저 와야 함
    }

    /**
     * QueryDSL 기반 테스트를 위한 {@link JPAQueryFactory} Bean 설정 클래스입니다.
     *
     * <p>
     * {@code @DataJpaTest}는 ComponentScan 범위를 제한하기 때문에
     * {@link com.nhnacademy.workanalysis.repository.custom.impl.AiChatThreadRepositoryImpl}
     * 클래스에서 JPAQueryFactory를 주입받기 위해 별도로 정의해야 합니다.
     *
     * 이 설정 클래스는 테스트 컨텍스트 내부에서만 적용됩니다.
     */
    @TestConfiguration
    static class QueryDslTestConfig {

        @PersistenceContext
        private EntityManager em;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }
}
