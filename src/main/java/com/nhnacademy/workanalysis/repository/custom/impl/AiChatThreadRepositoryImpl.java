package com.nhnacademy.workanalysis.repository.custom.impl;

import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.entity.QAiChatThread;
import com.nhnacademy.workanalysis.repository.custom.AiChatThreadRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL 기반 AI 분석 쓰레드 커스텀 리포지토리 구현 클래스입니다.
 */
@Repository
@RequiredArgsConstructor
public class AiChatThreadRepositoryImpl implements AiChatThreadRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 사원 번호 기준 전체 쓰레드를 생성일 내림차순으로 조회합니다.
     *
     * @param mbNo 사원 번호
     * @return 해당 사원의 쓰레드 목록
     */
    @Override
    public List<AiChatThread> findThreadsByMbNoDesc(Long mbNo) {
        QAiChatThread thread = QAiChatThread.aiChatThread;

        return queryFactory
                .selectFrom(thread)
                .where(thread.mbNo.eq(mbNo))
                .orderBy(thread.createdAt.desc())
                .fetch();
    }

    /**
     * 사원 번호 기준 최신 쓰레드를 최대 limit 개수만큼 조회합니다.
     *
     * @param mbNo  사원 번호
     * @param limit 최대 조회 개수
     * @return 해당 사원의 쓰레드 목록 (최신순, 제한 있음)
     */
    @Override
    public List<AiChatThread> findThreadsByMbNoDesc(Long mbNo, int limit) {
        QAiChatThread thread = QAiChatThread.aiChatThread;

        return queryFactory
                .selectFrom(thread)
                .where(thread.mbNo.eq(mbNo))
                .orderBy(thread.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
