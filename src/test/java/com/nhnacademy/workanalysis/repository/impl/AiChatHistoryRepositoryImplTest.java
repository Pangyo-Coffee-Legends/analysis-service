package com.nhnacademy.workanalysis.repository.impl;

import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.repository.AiChatHistoryRepository;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "member.service.url=http://localhost:8080",
        "work.entry.service.url=http://localhost:8081"
})

class AiChatHistoryRepositoryImplTest {

    @Autowired
    private AiChatThreadRepository threadRepository;

    @Autowired
    private AiChatHistoryRepository historyRepository;

    @Test
    @DisplayName("QueryDSL - 쓰레드 기준 히스토리 조회")
    void findHistoriesByThreadIdDesc_success() {
        AiChatThread thread = AiChatThread.create(1L, "대화");
        threadRepository.save(thread);

        AiChatHistory h1 = AiChatHistory.of(thread, "user", "질문1");
        AiChatHistory h2 = AiChatHistory.of(thread, "ai", "응답1");

        historyRepository.saveAll(List.of(h1, h2));

        List<AiChatHistory> result = historyRepository.findHistoriesByThreadIdDesc(thread.getThreadId());

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getRole()).isIn("ai", "user");
    }

    /**
     * 테스트 환경에서 QueryDSL의 JPAQueryFactory 빈을 수동 등록합니다.
     */
    @org.springframework.boot.test.context.TestConfiguration
    static class QueryDslTestConfig {

        @Autowired
        private EntityManager entityManager;

        @org.springframework.context.annotation.Bean
        public com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory() {
            return new com.querydsl.jpa.impl.JPAQueryFactory(entityManager);
        }
    }
}
