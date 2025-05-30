package com.nhnacademy.workanalysis.repository.custom.impl;

import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.QAiChatHistory;
import com.nhnacademy.workanalysis.repository.custom.AiChatHistoryRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL을 사용하여 AiChatHistoryRepositoryCustom의 메서드를 구현한 클래스입니다.
 * Q클래스를 통해 type-safe한 쿼리를 작성하며, Spring Bean으로 등록됩니다.
 */
@Repository
@RequiredArgsConstructor
public class AiChatHistoryRepositoryImpl implements AiChatHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * Q클래스를 활용하여 주어진 쓰레드 ID에 해당하는 히스토리를 생성일 기준으로 내림차순 정렬하여 조회합니다.
     *
     * @param threadId 조회할 쓰레드 ID
     * @return 해당 쓰레드에 속한 대화 히스토리 리스트
     */
    @Override
    public List<AiChatHistory> findHistoriesByThreadIdDesc(Long threadId) {
        QAiChatHistory aiChatHistory = QAiChatHistory.aiChatHistory;

        return queryFactory
                .selectFrom(aiChatHistory)
                .where(aiChatHistory.thread.threadId.eq(threadId))
                .orderBy(aiChatHistory.createdAt.desc())
                .fetch();
    }
}
