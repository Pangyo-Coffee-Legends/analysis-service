package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.repository.custom.AiChatHistoryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * AI 대화 히스토리(AiChatHistory) 엔티티에 대한 JPA 기본 + QueryDSL 커스텀 리포지토리입니다.
 */
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long>, AiChatHistoryRepositoryCustom {

    /**
     * Spring Data JPA 방식으로 쓰레드 ID 기준 최신순 100개 한정 조회
     * 무한으로 지정할 경우 GPT처럼 속도 저하 및 비용 문제 등으로 인한 한정 처리
     *
     * @param threadId 쓰레드 ID
     * @return 해당 쓰레드에 속한 대화 리스트
     */
    List<AiChatHistory> findTop100ByThreadThreadIdOrderByCreatedAtDesc(Long threadId);
}
