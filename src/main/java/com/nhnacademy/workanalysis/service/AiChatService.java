package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.dto.*;

import java.util.List;

/**
 * Gemini AI 기반 분석 및 대화 쓰레드/히스토리 관리 기능을 정의하는 서비스 인터페이스입니다.
 * 이 인터페이스는 컨트롤러에서 호출되는 주요 비즈니스 로직의 계약을 정의합니다.
 */
public interface AiChatService {

    /**
     * Gemini API를 호출하여 사용자의 질문 및 근무 데이터를 기반으로 AI 분석 결과를 반환합니다.
     *
     * @param request 분석 요청 데이터 (대화 메시지, 사원번호, 근무기록 등 포함)
     * @return Gemini API의 분석 응답 결과 (마크다운 형식 텍스트 포함)
     */
    GeminiAnalysisResponse analyze(GeminiAnalysisRequest request);

    /**
     * 새로운 대화 쓰레드를 생성합니다.
     *
     * @param mbNo  쓰레드를 생성할 사원의 고유 번호
     * @param title 생성할 쓰레드의 제목
     * @return 생성된 AiChatThread DTO 객체
     */
    AiChatThreadDto createThread(Long mbNo, String title);

    /**
     * 특정 쓰레드에 대화 히스토리(메시지)를 저장합니다.
     *
     * @param threadId 쓰레드 ID
     * @param role     메시지를 보낸 주체 ("user" 또는 "ai")
     * @param content  메시지 본문 내용
     * @return 저장된 AiChatHistory DTO 객체
     */
    AiChatHistoryDto saveHistory(Long threadId, String role, String content);

    /**
     * 특정 사원(mbNo)의 전체 대화 쓰레드 목록을 조회합니다.
     *
     * @param mbNo 사원 고유 번호
     * @return 해당 사원의 쓰레드 DTO 목록 (최신순 정렬된 리스트)
     */
    List<AiChatThreadDto> getThreadsByMember(Long mbNo);

    /**
     * 특정 쓰레드에 속한 전체 대화 히스토리를 조회합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 해당 쓰레드의 전체 대화 히스토리 DTO 리스트 (최신순 정렬)
     */
    List<AiChatHistoryDto> getHistoriesByThread(Long threadId);

    /**
     * 특정 쓰레드를 삭제합니다.
     *
     * @param threadId 삭제할 쓰레드 ID
     */
    void deleteThread(Long threadId);

    /**
     * 특정 쓰레드의 제목을 수정합니다.
     *
     * @param threadId 수정 대상 쓰레드 ID
     * @param newTitle 새로운 쓰레드 제목
     */
    void updateThreadTitle(Long threadId, String newTitle);

    /**
     * 특정 쓰레드의 히스토리를 DTO 리스트로 반환합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 히스토리 DTO 리스트
     */
    List<AiChatHistoryDto> getHistoryDtoList(Long threadId);

    /**
     * 대화 메시지 저장 전 유효성 검사 및 변환을 수행하고 저장합니다.
     *
     * @param request 대화 저장 요청 DTO
     * @return 저장된 히스토리 DTO 객체
     */
    AiChatHistoryDto saveValidatedMessage(AiChatHistorySaveRequest request);

    GeminiAnalysisResponse generateReport(ReportRequestDto request);

}
