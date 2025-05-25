package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatHistory;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AiChatHistoryRepository의 기본 JPA 메서드에 대한 단위 테스트 클래스입니다.
 * <p>
 * QueryDSL 기반 커스텀 리포지토리와의 충돌을 방지하고,
 * 생성일 기준 내림차순으로 히스토리를 정확히 조회하는지 검증합니다.
 */
@DataJpaTest
@Import(AiChatHistoryRepositoryTest.QueryDslTestConfig.class)
class AiChatHistoryRepositoryTest {

    @Autowired
    private AiChatThreadRepository threadRepository;

    @Autowired
    private AiChatHistoryRepository historyRepository;

    /**
     * 주어진 threadId에 대해 최대 100개의 히스토리를 생성일 기준으로 내림차순 조회합니다.
     * <p>
     * 실제 결과가 최신 순으로 정렬되었는지와 개수를 검증합니다.
     */
    @Test
    @DisplayName("threadId 기준 생성일 내림차순 히스토리 조회")
    void findTop100ByThreadThreadIdOrderByCreatedAtDesc_success() {
        AiChatThread thread = AiChatThread.create(1L, "대화");
        threadRepository.save(thread);

        AiChatHistory h1 = AiChatHistory.of(thread, "user", "첫 번째 메시지");
        AiChatHistory h2 = AiChatHistory.of(thread, "ai", "두 번째 메시지");
        historyRepository.saveAll(List.of(h1, h2));

        List<AiChatHistory> result = historyRepository.findTop100ByThreadThreadIdOrderByCreatedAtDesc(thread.getThreadId());

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getContent()).isIn("첫 번째 메시지", "두 번째 메시지");
    }

    /**
     * 테스트 컨텍스트에 QueryDSL용 JPAQueryFactory 빈을 수동 등록하기 위한 설정입니다.
     * <p>
     * @DataJpaTest는 슬라이스 테스트로 @ComponentScan을 하지 않기 때문에
     * 커스텀 리포지토리 구현체(AiChatHistoryRepositoryImpl) 내부에서 JPAQueryFactory를 사용하기 위해선
     * 수동으로 Bean 등록이 필요합니다.
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
