package com.nhnacademy.workanalysis.service.impl;

import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.exception.ThreadNotFoundException;
import com.nhnacademy.workanalysis.repository.AiChatHistoryRepository;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import com.nhnacademy.workanalysis.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 분석 서비스의 구현체 클래스입니다.
 * Gemini API를 활용한 분석 요청 및 쓰레드/히스토리 관리를 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final AiChatApiClient aiChatApiClient;
    private final AiChatThreadRepository aiChatThreadRepository;
    private final AiChatHistoryRepository aiChatHistoryRepository;

    /**
     * Gemini API를 호출하여 분석을 수행합니다.
     *
     * @param request 분석 요청 메시지
     * @return 분석 결과
     */
    @Override
    public GeminiAnalysisResponse analyze(GeminiAnalysisRequest request) {
        log.info("분석 요청 시작 - memberNo: {}", request.getMemberNo());
        return aiChatApiClient.call(request.getMessages(), request.getMemberNo());
    }

    /**
     * 새로운 쓰레드를 생성합니다.
     *
     * @param mbNo  회원 번호
     * @param title 쓰레드 제목
     * @return 생성된 쓰레드
     */
    @Override
    public AiChatThread createThread(Long mbNo, String title) {
        AiChatThread aiChatThread = new AiChatThread();
        aiChatThread.setMbNo(mbNo);
        aiChatThread.setTitle(title);
        AiChatThread saved = aiChatThreadRepository.save(aiChatThread);
        log.info("새 쓰레드 생성 - threadId: {}, mbNo: {}", saved.getThreadId(), mbNo);
        return saved;
    }

    /**
     * 쓰레드 제목을 변경합니다.
     *
     * @param threadId  쓰레드 ID
     * @param newTitle  새로운 제목
     */
    @Override
    public void updateThreadTitle(Long threadId, String newTitle) {
        log.debug("쓰레드 제목 변경 시도 - threadId: {}", threadId);
        AiChatThread thread = aiChatThreadRepository.findById(threadId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 쓰레드 접근 - threadId: {}", threadId);
                    return new ThreadNotFoundException("존재하지 않는 쓰레드입니다.");
                });
        thread.setTitle(newTitle);
        aiChatThreadRepository.save(thread);
        log.info("쓰레드 제목 변경 완료 - threadId: {}, newTitle: {}", threadId, newTitle);
    }

    /**
     * 쓰레드를 삭제합니다.
     *
     * @param threadId 쓰레드 ID
     */
    @Override
    public void deleteThread(Long threadId) {
        log.debug("쓰레드 삭제 시도 - threadId: {}", threadId);
        if (!aiChatThreadRepository.existsById(threadId)) {
            log.warn("삭제 요청된 쓰레드가 존재하지 않음 - threadId: {}", threadId);
            throw new ThreadNotFoundException("삭제할 쓰레드가 존재하지 않습니다.");
        }
        aiChatThreadRepository.deleteById(threadId);
        log.info("쓰레드 삭제 완료 - threadId: {}", threadId);
    }

    /**
     * 채팅 히스토리를 저장합니다.
     *
     * @param threadId 쓰레드 ID
     * @param role     역할 (예: user, assistant)
     * @param content  메시지 내용
     * @return 저장된 히스토리
     */
    @Override
    public AiChatHistory saveHistory(Long threadId, String role, String content) {
        log.debug("히스토리 저장 시도 - threadId: {}, role: {}", threadId, role);
        AiChatThread thread = aiChatThreadRepository.findById(threadId)
                .orElseThrow(() -> {
                    log.error("히스토리 저장 중 쓰레드 찾기 실패 - threadId: {}", threadId);
                    return new ThreadNotFoundException("존재하지 않는 쓰레드입니다.");
                });
        AiChatHistory history = new AiChatHistory();
        history.setThread(thread);
        history.setRole(role);
        history.setContent(content);
        AiChatHistory saved = aiChatHistoryRepository.save(history);
        log.info("히스토리 저장 완료 - historyId: {}, threadId: {}", saved.getHistoryId(), threadId);
        return saved;
    }

    /**
     * 특정 회원의 모든 쓰레드 목록을 조회합니다.
     *
     * @param mbNo 회원 번호
     * @return 쓰레드 목록
     */
    @Override
    public List<AiChatThread> getThreadsByMember(Long mbNo) {
        log.debug("회원의 쓰레드 목록 조회 - mbNo: {}", mbNo);
        return aiChatThreadRepository.findByMbNoOrderByCreatedAtDesc(mbNo);
    }

    /**
     * 특정 쓰레드에 대한 모든 히스토리를 조회합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 히스토리 목록
     */
    @Override
    public List<AiChatHistory> getHistoriesByThread(Long threadId) {
        log.debug("히스토리 조회 시도 - threadId: {}", threadId);
        return aiChatHistoryRepository.findByThread_ThreadIdOrderByCreatedAtDesc(threadId);
    }
}
