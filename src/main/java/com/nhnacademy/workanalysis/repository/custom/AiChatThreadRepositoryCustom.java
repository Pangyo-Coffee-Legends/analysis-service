package com.nhnacademy.workanalysis.repository.custom;

import com.nhnacademy.workanalysis.entity.AiChatThread;

import java.util.List;

/**
 * QueryDSL 기반 AI 분석 쓰레드(AiChatThread) 커스텀 리포지토리입니다.
 * 복합 조건이나 개수 제한 조회 시 사용합니다.
 */
public interface AiChatThreadRepositoryCustom {

    /**
     * 특정 사원의 전체 쓰레드를 생성일 기준 내림차순으로 조회합니다.
     *
     * @param mbNo 사원 번호
     * @return 해당 사원의 전체 쓰레드 목록
     */
    List<AiChatThread> findThreadsByMbNoDesc(Long mbNo);

    /**
     * 특정 사원의 쓰레드를 생성일 기준 내림차순으로 limit 개수만큼 조회합니다.
     *
     * @param mbNo  사원 번호
     * @param limit 최대 조회 개수
     * @return 해당 사원의 쓰레드 목록 (최신순, 최대 limit개)
     */
    List<AiChatThread> findThreadsByMbNoDesc(Long mbNo, int limit);
}
