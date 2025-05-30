package com.nhnacademy.workanalysis.repository.custom;

import com.nhnacademy.workanalysis.entity.AiChatHistory;

import java.util.List;

/**
 * QueryDSL을 사용한 AiChatHistory 커스텀 리포지토리 인터페이스입니다.
 * 복잡한 조건의 쿼리 구현 시 사용됩니다.
 */
public interface AiChatHistoryRepositoryCustom {

    /**
     * 특정 쓰레드 ID에 속한 대화 히스토리를 생성일 기준으로 내림차순 정렬하여 조회합니다.
     *
     * @param threadId 조회할 쓰레드 ID
     * @return 해당 쓰레드에 속한 히스토리 리스트 (최신순 정렬)
     */
    List<AiChatHistory> findHistoriesByThreadIdDesc(Long threadId);
}
