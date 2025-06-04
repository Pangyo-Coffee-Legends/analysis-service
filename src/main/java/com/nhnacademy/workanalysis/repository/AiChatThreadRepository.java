package com.nhnacademy.workanalysis.repository;

import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.repository.custom.AiChatThreadRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * AI 분석 쓰레드(AiChatThread) 엔티티에 대한 데이터 접근을 처리하는 JPA 레포지토리 인터페이스입니다.
 * 기본적인 CRUD 메서드는 JpaRepository에서 제공되며,
 * 특정 사원(mbNo)의 쓰레드를 생성일 기준으로 정렬하여 조회하는 메서드를 제공합니다.
 */
public interface AiChatThreadRepository extends JpaRepository<AiChatThread, Long>, AiChatThreadRepositoryCustom {

    /**
     * 특정 사원 번호(mbNo)에 해당하는 모든 쓰레드를 생성일 기준으로 내림차순 정렬하여 조회합니다.
     *
     * @param mbNo 사원 고유 번호
     * @return 해당 사원의 전체 쓰레드 목록 (최신순 정렬)
     */
    List<AiChatThread> findByMbNoOrderByCreatedAtDesc(Long mbNo);
}
