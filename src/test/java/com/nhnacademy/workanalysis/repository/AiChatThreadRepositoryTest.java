package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatThread;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AiChatThreadRepositoryTest {

    @Autowired
    private AiChatThreadRepository threadRepository;

    @Test
    @DisplayName("사원 번호로 쓰레드를 생성일 역순으로 조회")
    void findByMbNoOrderByCreatedAtDesc() {
        AiChatThread t1 = new AiChatThread();
        t1.setMbNo(100L);
        t1.setTitle("첫 번째");
        t1.setCreatedAt(LocalDateTime.now().minusDays(1));

        AiChatThread t2 = new AiChatThread();
        t2.setMbNo(100L);
        t2.setTitle("두 번째");
        t2.setCreatedAt(LocalDateTime.now());

        threadRepository.saveAll(List.of(t1, t2));

        List<AiChatThread> result = threadRepository.findByMbNoOrderByCreatedAtDesc(100L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("두 번째");
        assertThat(result.get(1).getTitle()).isEqualTo("첫 번째");
    }
}
