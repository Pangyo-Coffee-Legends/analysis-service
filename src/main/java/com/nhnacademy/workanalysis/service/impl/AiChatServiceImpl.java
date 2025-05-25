package com.nhnacademy.workanalysis.service.impl;

import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.dto.*;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.exception.AiChatThreadNotFoundException;
import com.nhnacademy.workanalysis.repository.AiChatHistoryRepository;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import com.nhnacademy.workanalysis.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gemini AI 기반 대화 분석 서비스 구현체입니다.
 * 쓰레드 및 히스토리 저장, 분석 API 연동, DTO 변환을 포함한 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {

    private final AiChatApiClient aiChatApiClient;
    private final AiChatThreadRepository aiChatThreadRepository;
    private final AiChatHistoryRepository aiChatHistoryRepository;

    /**
     * Gemini API를 호출하여 분석 요청을 처리합니다.
     *
     * @param request 분석 요청 데이터 (사원 번호, 메시지 등 포함)
     * @return 분석 응답 결과 (마크다운 텍스트 포함)
     */
    @Override
    public GeminiAnalysisResponse analyze(GeminiAnalysisRequest request) {
        log.info("분석 요청 시작 - memberNo: {}", request.getMemberNo());
        return aiChatApiClient.call(request.getMessages(), request.getMemberNo());
    }

    /**
     * 새로운 대화 쓰레드를 생성하고 DTO로 반환합니다.
     *
     * @param mbNo  사원 번호
     * @param title 쓰레드 제목
     * @return 생성된 쓰레드 DTO
     */
    @Override
    @Transactional
    public AiChatThreadDto createThread(Long mbNo, String title) {
        AiChatThread aiChatThread = AiChatThread.create(mbNo, title);
        AiChatThread saved = aiChatThreadRepository.save(aiChatThread);
        log.info("새 쓰레드 생성 - threadId: {}, mbNo: {}", saved.getThreadId(), mbNo);
        return toThreadDto(saved);
    }

    /**
     * 특정 쓰레드의 제목을 수정합니다.
     *
     * @param threadId 쓰레드 ID
     * @param newTitle 새로운 제목
     */
    @Override
    @Transactional
    public void updateThreadTitle(Long threadId, String newTitle) {

        AiChatThread thread = getThreadOrThrow(threadId);
        log.error("존재하지 않는 쓰레드 접근 - threadId: {}", threadId);

        thread.updateTitle(newTitle);
        aiChatThreadRepository.save(thread);
        log.info("쓰레드 제목 변경 완료 - threadId: {}, newTitle: {}", threadId, newTitle);
    }

    /**
     * 특정 쓰레드를 삭제합니다.
     *
     * @param threadId 삭제할 쓰레드 ID
     */
    @Override
    @Transactional
    public void deleteThread(Long threadId) {
        if (!aiChatThreadRepository.existsById(threadId)) {
            log.warn("삭제 요청된 쓰레드가 존재하지 않음 - threadId: {}", threadId);
            throw new AiChatThreadNotFoundException("삭제할 쓰레드가 존재하지 않습니다.");
        }
        aiChatThreadRepository.deleteById(threadId);
        log.info("쓰레드 삭제 완료 - threadId: {}", threadId);
    }

    /**
     * 대화 히스토리를 저장하고 DTO로 반환합니다.
     *
     * @param threadId 쓰레드 ID
     * @param role     메시지 작성자 역할
     * @param content  메시지 내용
     * @return 저장된 히스토리 DTO
     */
    @Override
    @Transactional
    public AiChatHistoryDto saveHistory(Long threadId, String role, String content) {

        AiChatThread thread = getThreadOrThrow(threadId);

        AiChatHistory history = AiChatHistory.of(thread, role, content);
        AiChatHistory saved = aiChatHistoryRepository.save(history);
        log.info("히스토리 저장 완료 - historyId: {}, threadId: {}", saved.getHistoryId(), threadId);
        return toHistoryDto(saved);
    }

    /**
     * 특정 사원의 쓰레드 목록을 조회하고 DTO 리스트로 반환합니다.
     *
     * @param mbNo 사원 번호
     * @return 쓰레드 DTO 리스트
     */
    @Override
    public List<AiChatThreadDto> getThreadsByMember(Long mbNo) {
        return aiChatThreadRepository.findByMbNoOrderByCreatedAtDesc(mbNo)
                .stream()
                .map(this::toThreadDto)
                .toList();
    }

    /**
     * 특정 쓰레드에 대한 히스토리를 조회하고 DTO 리스트로 반환합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 히스토리 DTO 리스트
     */
    @Override
    public List<AiChatHistoryDto> getHistoriesByThread(Long threadId) {
        return aiChatHistoryRepository.findTop100ByThreadThreadIdOrderByCreatedAtDesc(threadId)
                .stream()
                .map(this::toHistoryDto)
                .toList();
    }

    /**
     * 특정 쓰레드의 히스토리를 DTO 리스트로 반환합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 히스토리 DTO 리스트
     */
    @Override
    public List<AiChatHistoryDto> getHistoryDtoList(Long threadId) {
        return getHistoriesByThread(threadId);
    }

    /**
     * 대화 저장 요청 DTO를 검증하고 저장한 후 DTO로 반환합니다.
     *
     * @param request 대화 저장 요청 DTO
     * @return 저장된 히스토리 DTO
     */
    @Override
    public AiChatHistoryDto saveValidatedMessage(AiChatHistorySaveRequest request) {
        String content = request.getContent();
        Long threadId = request.getThreadId();
        String role = request.getRole();

        if (content == null || content.trim().isEmpty()) {
            log.error("❌ [대화 저장 실패] content 값이 null 또는 빈 문자열입니다. threadId={}, role={}", threadId, role);
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }

        log.info("📝 [대화 저장 요청] threadId={}, role={}, content length={}", threadId, role, content.length());
        return saveHistory(threadId, role, content);
    }


    /**
     * 엔티티 → DTO 변환 (AiChatThread)
     */
    private AiChatThreadDto toThreadDto(AiChatThread thread) {
        return new AiChatThreadDto(
                thread.getThreadId(),
                thread.getMbNo(),
                thread.getTitle(),
                thread.getCreatedAt()
        );
    }

    /**
     * 엔티티 → DTO 변환 (AiChatHistory)
     */
    private AiChatHistoryDto toHistoryDto(AiChatHistory history) {
        return new AiChatHistoryDto(
                history.getHistoryId(),
                history.getRole(),
                history.getContent(),
                history.getCreatedAt()
        );
    }

    /**
     * 주어진 쓰레드 ID로 쓰레드를 조회하고, 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param threadId 조회할 쓰레드 ID
     * @return 조회된 AiChatThread 엔티티
     * @throws AiChatThreadNotFoundException 존재하지 않을 경우 발생
     */
    private AiChatThread getThreadOrThrow(Long threadId) {
        return aiChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new AiChatThreadNotFoundException("존재하지 않는 쓰레드입니다. threadId=" + threadId));
    }

}
