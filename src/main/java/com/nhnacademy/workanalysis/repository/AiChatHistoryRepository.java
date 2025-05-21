package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * AI 대화 히스토리(AiChatHistory) 엔티티에 대한 데이터 접근을 처리하는 JPA 레포지토리 인터페이스입니다.
 * 기본적인 CRUD 메서드는 JpaRepository에서 제공하며,
 * 추가적으로 특정 쓰레드 ID에 속한 히스토리를 생성일 기준으로 역순 조회하는 기능이 정의되어 있습니다.
 */
public interface AiChatHistoryRepository extends JpaRepository<AiChatHistory, Long> {

    /**
     * 특정 쓰레드 ID에 속한 모든 대화 히스토리를 생성일 기준으로 내림차순 정렬하여 조회합니다.
     *
     * @param threadId 조회할 쓰레드 ID
     * @return 해당 쓰레드에 속한 히스토리 리스트 (최신순 정렬)
     */
    List<AiChatHistory> findByThread_ThreadIdOrderByCreatedAtDesc(Long threadId);
}
