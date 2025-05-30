package com.nhnacademy.workanalysis.service.impl;

import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.adaptor.MemberServiceClient;
import com.nhnacademy.workanalysis.adaptor.WorkEntryClient;
import com.nhnacademy.workanalysis.dto.*;
import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
import com.nhnacademy.workanalysis.dto.attendance.MemberInfoResponse;
import com.nhnacademy.workanalysis.dto.attendance.MemberPageResponse;
import com.nhnacademy.workanalysis.dto.attendance.PageResponse;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.exception.AiChatThreadNotFoundException;
import com.nhnacademy.workanalysis.exception.WorkEntryRecordNotFoundException;
import com.nhnacademy.workanalysis.repository.AiChatHistoryRepository;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import com.nhnacademy.workanalysis.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Gemini AI 기반 근태 분석 서비스 구현체입니다.
 * <p>다음 기능을 제공합니다:
 * <ul>
 *     <li>쓰레드 생성/수정/삭제</li>
 *     <li>메시지 저장 및 유효성 검증</li>
 *     <li>Gemini API 호출 및 리포트 분석</li>
 *     <li>근무 기록 프롬프트 변환</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {

    private final AiChatApiClient aiChatApiClient;
    private final AiChatThreadRepository aiChatThreadRepository;
    private final AiChatHistoryRepository aiChatHistoryRepository;
    private final WorkEntryClient workEntryClient;
    private final MemberServiceClient memberServiceClient;

    private static final Map<String, String> STATUS_CODE_MAP = Map.of(
            "1", "출근", "2", "지각", "3", "결근", "4", "외근",
            "5", "연차", "6", "질병", "7", "반차", "8", "상"
    );

    /**
     * 사원의 월별 출결 데이터를 Gemini에 분석 요청합니다.
     *
     * @param request 분석할 사원의 출결 요청 정보
     * @return GeminiAnalysisResponse 분석 결과
     * @throws WorkEntryRecordNotFoundException 출결 데이터가 존재하지 않을 경우 예외 발생
     */
    @Override
    public GeminiAnalysisResponse generateReport(ReportRequestDto request) {
        Long mbNo = request.getMbNo();
        Integer year = request.getYear();
        Integer month = request.getMonth();

        log.debug("[generateReport] 분석 대상 - mbNo: {}, year: {}, month: {}", mbNo, year, month);

        MemberPageResponse memberPage = memberServiceClient.getMemberInfoList(0, 1000);
        String mbName = memberPage.getContent().stream()
                .filter(m -> m.getNo().equals(mbNo))
                .map(MemberInfoResponse::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 사원을 찾을 수 없습니다."));

        PageResponse<AttendanceSummaryDto> pageResponse = workEntryClient.getRecent30DaySummary(mbNo);
        List<WorkRecordDto> workRecords = pageResponse.getContent().stream()
                .filter(s -> s.getYear() == year && s.getMonthValue() == month)
                .map(summary -> new WorkRecordDto(
                        LocalDate.of(summary.getYear(), summary.getMonthValue(), summary.getDayOfMonth()).toString(),
                        getDayOfWeek(summary.getInTime(), summary.getOutTime()),
                        summary.getCode().toString(),
                        summary.getInTime() != null ? summary.getInTime().toLocalTime().toString() : null,
                        summary.getOutTime() != null ? summary.getOutTime().toLocalTime().toString() : null
                ))
                .toList();

        if (workRecords.isEmpty()) {
            log.warn("근무 기록 없음 - mbNo: {}, year: {}, month: {}", mbNo, year, month);
            throw new WorkEntryRecordNotFoundException(mbNo, year, month);
        }

        log.info("Gemini 분석용 프롬프트 생성 시작 - 데이터 {}건", workRecords.size());

        List<MessageDto> messages = List.of(
                new MessageDto("user", String.format("%s 사원의 %d년 %d월 근무 기록 요약을 요청합니다.", mbName, year, month)),
                new MessageDto("user", formatRecordsToPrompt(workRecords))
        );

        GeminiAnalysisRequest analysisRequest = new GeminiAnalysisRequest(mbNo, messages, workRecords);
        return analyze(analysisRequest);
    }

    /**
     * 출/퇴근 시간 중 유효한 시간으로 요일을 구합니다.
     *
     * @param in 출근 시간
     * @param out 퇴근 시간
     * @return 요일 문자열, 유효한 시간 없을 시 "정보없음"
     */
    private String getDayOfWeek(LocalDateTime in, LocalDateTime out) {
        LocalDateTime valid = in != null ? in : out;
        if (valid == null) {
            log.warn("요일 정보 없음 - inTime, outTime 모두 null");
            return "정보없음";
        }
        return valid.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
    }

    /**
     * 근무 기록 리스트를 분석용 문자열 프롬프트로 변환합니다.
     *
     * @param records 분석할 근무 기록 목록
     * @return Gemini 프롬프트 문자열
     */
    private String formatRecordsToPrompt(List<WorkRecordDto> records) {
        StringBuilder sb = new StringBuilder();
        for (WorkRecordDto r : records) {
            String statusDesc = STATUS_CODE_MAP.getOrDefault(r.getStatusCode(), "기타");
            sb.append("- ").append(r.getDate())
                    .append(" (").append(r.getDayOfWeek()).append("): ").append(statusDesc);
            if (r.getInTime() != null) sb.append(", 출근 ").append(r.getInTime());
            if (r.getOutTime() != null) sb.append(", 퇴근 ").append(r.getOutTime());
            sb.append("\n");
        }
        log.debug("Gemini 프롬프트: {}", sb);
        return sb.toString();
    }

    /**
     * Gemini API를 호출하여 분석 결과를 반환합니다.
     *
     * @param request Gemini 분석 요청 객체
     * @return 분석 결과 DTO
     */
    @Override
    public GeminiAnalysisResponse analyze(GeminiAnalysisRequest request) {
        log.info("분석 요청 시작 - memberNo: {}", request.getMemberNo());
        return aiChatApiClient.call(request.getMessages(), request.getMemberNo());
    }

    /**
     * 새로운 분석 쓰레드를 생성합니다.
     *
     * @param mbNo 사원 번호
     * @param title 쓰레드 제목
     * @return 생성된 쓰레드 DTO
     */
    @Override
    @Transactional
    public AiChatThreadDto createThread(Long mbNo, String title) {
        log.debug("쓰레드 생성 요청 - mbNo: {}, title: {}", mbNo, title);
        AiChatThread thread = AiChatThread.create(mbNo, title);
        return toThreadDto(aiChatThreadRepository.save(thread));
    }

    /**
     * 쓰레드 제목을 수정합니다.
     *
     * @param threadId 수정할 쓰레드 ID
     * @param newTitle 새 제목
     */
    @Override
    @Transactional
    public void updateThreadTitle(Long threadId, String newTitle) {
        log.info("쓰레드 제목 수정 요청 - threadId: {}, newTitle: {}", threadId, newTitle);
        AiChatThread thread = getThreadOrThrow(threadId);
        thread.updateTitle(newTitle);
        aiChatThreadRepository.save(thread);
    }

    /**
     * 쓰레드를 삭제합니다.
     *
     * @param threadId 삭제할 쓰레드 ID
     * @throws AiChatThreadNotFoundException 쓰레드가 존재하지 않을 경우
     */
    @Override
    @Transactional
    public void deleteThread(Long threadId) {
        log.warn("쓰레드 삭제 요청 - threadId: {}", threadId);
        if (!aiChatThreadRepository.existsById(threadId)) {
            log.error("존재하지 않는 쓰레드 삭제 시도 - threadId: {}", threadId);
            throw new AiChatThreadNotFoundException();
        }
        aiChatThreadRepository.deleteById(threadId);
    }

    /**
     * 쓰레드에 메시지를 저장합니다.
     *
     * @param threadId 대상 쓰레드 ID
     * @param role 발신자 역할
     * @param content 메시지 내용
     * @return 저장된 메시지 DTO
     */
    @Override
    @Transactional
    public AiChatHistoryDto saveHistory(Long threadId, String role, String content) {
        log.debug("히스토리 저장 - threadId: {}, role: {}", threadId, role);
        AiChatThread thread = getThreadOrThrow(threadId);
        AiChatHistory history = AiChatHistory.of(thread, role, content);
        return toHistoryDto(aiChatHistoryRepository.save(history));
    }

    /**
     * 사원별 쓰레드 목록을 조회합니다.
     *
     * @param mbNo 사원 번호
     * @return 쓰레드 DTO 목록
     */
    @Override
    public List<AiChatThreadDto> getThreadsByMember(Long mbNo) {
        return aiChatThreadRepository.findByMbNoOrderByCreatedAtDesc(mbNo)
                .stream().map(this::toThreadDto).toList();
    }

    /**
     * 쓰레드별 최근 메시지 100개를 조회합니다.
     *
     * @param threadId 대상 쓰레드 ID
     * @return 메시지 DTO 목록
     */
    @Override
    public List<AiChatHistoryDto> getHistoriesByThread(Long threadId) {
        return aiChatHistoryRepository.findTop100ByThreadThreadIdOrderByCreatedAtDesc(threadId)
                .stream().map(this::toHistoryDto).toList();
    }

    /**
     * 히스토리 조회 래퍼 메서드입니다.
     *
     * @param threadId 쓰레드 ID
     * @return 메시지 DTO 목록
     */
    @Override
    public List<AiChatHistoryDto> getHistoryDtoList(Long threadId) {
        return getHistoriesByThread(threadId);
    }

    /**
     * 메시지 유효성 검증 후 저장합니다.
     *
     * @param request 저장 요청 객체
     * @return 저장된 메시지 DTO
     * @throws IllegalArgumentException 메시지 내용이 비어있을 경우
     */
    @Override
    public AiChatHistoryDto saveValidatedMessage(AiChatHistorySaveRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            log.error("빈 메시지 저장 시도 - threadId: {}", request.getThreadId());
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
        return saveHistory(request.getThreadId(), request.getRole(), request.getContent());
    }


    /**
     * AiChatThread 엔티티를 AiChatThreadDto로 변환합니다.
     *
     * @param thread 변환할 쓰레드 엔티티
     * @return 변환된 쓰레드 DTO
     */
    private AiChatThreadDto toThreadDto(AiChatThread thread) {
        return new AiChatThreadDto(thread.getThreadId(), thread.getMbNo(), thread.getTitle(), thread.getCreatedAt());
    }

    /**
     * AiChatHistory 엔티티를 AiChatHistoryDto로 변환합니다.
     *
     * @param history 변환할 히스토리 엔티티
     * @return 변환된 히스토리 DTO
     */
    private AiChatHistoryDto toHistoryDto(AiChatHistory history) {
        return new AiChatHistoryDto(history.getHistoryId(), history.getRole(), history.getContent(), history.getCreatedAt());
    }
    /**
     * 쓰레드 ID로 쓰레드를 조회하거나 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param threadId 조회할 쓰레드 ID
     * @return 조회된 쓰레드 엔티티
     * @throws AiChatThreadNotFoundException 쓰레드가 존재하지 않을 경우 발생
     */
    private AiChatThread getThreadOrThrow(Long threadId) {
        return aiChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new AiChatThreadNotFoundException("존재하지 않는 쓰레드입니다. threadId=" + threadId));
    }
}
