package com.nhnacademy.workanalysis.repository.impl;

import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "member.service.url=http://localhost:8080",
        "work.entry.service.url=http://localhost:8081"
})

class AiChatThreadRepositoryImplTest {

    @Autowired
    private AiChatThreadRepository threadRepository;

    @TestConfiguration
    static class QueryDslTestConfig {
        @PersistenceContext
        private EntityManager em;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(em);
        }
    }

    @Test
    @DisplayName("QueryDSL - mbNo 기준 limit 조회 테스트")
    void findThreadsByMbNoDesc_withLimit_success() {
        // given
        threadRepository.saveAll(List.of(
                AiChatThread.create(1L, "a"),
                AiChatThread.create(1L, "b"),
                AiChatThread.create(1L, "c")
        ));

        // when
        List<AiChatThread> result = threadRepository.findThreadsByMbNoDesc(1L, 2);  // 커스텀 메서드 직접 사용

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMbNo()).isEqualTo(1L);
    }
}
