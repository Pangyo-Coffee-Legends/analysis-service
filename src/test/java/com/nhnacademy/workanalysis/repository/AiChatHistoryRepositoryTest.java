package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AiChatHistoryRepositoryTest {

    @Autowired
    private AiChatHistoryRepository historyRepository;

    @Autowired
    private AiChatThreadRepository threadRepository;

    @Test
    @DisplayName("특정 쓰레드 ID로 히스토리를 생성일 역순으로 조회")
    void findByThread_ThreadIdOrderByCreatedAtDesc() {
        AiChatThread thread = new AiChatThread();
        thread.setMbNo(1L);
        thread.setTitle("Test Thread");
        threadRepository.save(thread);

        AiChatHistory h1 = new AiChatHistory(null, "user", "첫 질문", LocalDateTime.now().minusMinutes(10), thread);
        AiChatHistory h2 = new AiChatHistory(null, "ai", "첫 응답", LocalDateTime.now().minusMinutes(5), thread);
        AiChatHistory h3 = new AiChatHistory(null, "user", "두번째 질문", LocalDateTime.now(), thread);

        historyRepository.saveAll(List.of(h1, h2, h3));

        List<AiChatHistory> result = historyRepository.findByThread_ThreadIdOrderByCreatedAtDesc(thread.getThreadId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getContent()).isEqualTo("두번째 질문");
        assertThat(result.get(2).getContent()).isEqualTo("첫 질문");
    }
}
