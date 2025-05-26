package com.nhnacademy.workanalysis.service.impl;

import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.adaptor.WorkEntryClient;
import com.nhnacademy.workanalysis.dto.*;
import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Gemini AI 기반 근태 분석 서비스 구현체입니다.
 * 쓰레드 생성, 제목 수정, 히스토리 저장, 분석 요청, 리포트 생성 등을 포함한 전체 기능을 제공합니다.
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

    private static final Map<String, String> STATUS_CODE_MAP = Map.of(
            "1", "출근", "2", "지각", "3", "결근", "4", "외근",
            "5", "연차", "6", "질병", "7", "반차", "8", "상"
    );

    /**
     * 리포트 생성을 위한 AI 분석 요청을 수행하고 결과를 파일로 저장합니다.
     *
     * @param request 리포트 요청 DTO
     * @return 분석 결과
     */
    @Override
    public GeminiAnalysisResponse generateReport(ReportRequestDto request) {
        Long mbNo = request.getMbNo();
        Integer year = request.getYear();
        Integer month = request.getMonth();

        // 출결 요약 데이터 조회 (30일간)
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
            throw new WorkEntryRecordNotFoundException(mbNo, year, month);
        }

        List<MessageDto> messages = List.of(
                new MessageDto("user", String.format("%d번 사원의 %d년 %d월 근무 기록 요약을 요청합니다.", mbNo, year, month)),
                new MessageDto("user", formatRecordsToPrompt(workRecords))
        );

        GeminiAnalysisRequest analysisRequest = new GeminiAnalysisRequest(mbNo, messages, workRecords);
        GeminiAnalysisResponse result = analyze(analysisRequest);
        saveReportAsTextFile(mbNo, year, month, result.getFullText());

        return result;
    }

    /**
     * 출근 혹은 퇴근 시간 기준으로 요일을 추출합니다.
     * 둘 다 null인 경우 "정보없음" 반환
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
     * 분석용 프롬프트 텍스트를 생성합니다.
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
        return sb.toString();
    }

    /**
     * 분석 결과를 파일로 저장합니다.
     */
    private void saveReportAsTextFile(Long mbNo, int year, int month, String content) {
        try {
            String fileName = String.format("report_mb_%d_%04d_%02d.txt", mbNo, year, month);
            Path path = Path.of("/tmp", fileName);
            Files.writeString(path, content);
            log.info("📂 리포트 저장 완료: {}", path);
        } catch (IOException e) {
            log.warn("⚠️ 리포트 저장 실패: {}", e.getMessage());
        }
    }

    @Override
    public GeminiAnalysisResponse analyze(GeminiAnalysisRequest request) {
        log.info("분석 요청 시작 - memberNo: {}", request.getMemberNo());
        return aiChatApiClient.call(request.getMessages(), request.getMemberNo());
    }

    @Override
    @Transactional
    public AiChatThreadDto createThread(Long mbNo, String title) {
        AiChatThread thread = AiChatThread.create(mbNo, title);
        return toThreadDto(aiChatThreadRepository.save(thread));
    }

    @Override
    @Transactional
    public void updateThreadTitle(Long threadId, String newTitle) {
        AiChatThread thread = getThreadOrThrow(threadId);
        thread.updateTitle(newTitle);
        aiChatThreadRepository.save(thread);
    }

    @Override
    @Transactional
    public void deleteThread(Long threadId) {
        if (!aiChatThreadRepository.existsById(threadId)) {
            throw new AiChatThreadNotFoundException("삭제할 쓰레드가 존재하지 않습니다.");
        }
        aiChatThreadRepository.deleteById(threadId);
    }

    @Override
    @Transactional
    public AiChatHistoryDto saveHistory(Long threadId, String role, String content) {
        AiChatThread thread = getThreadOrThrow(threadId);
        AiChatHistory history = AiChatHistory.of(thread, role, content);
        return toHistoryDto(aiChatHistoryRepository.save(history));
    }

    @Override
    public List<AiChatThreadDto> getThreadsByMember(Long mbNo) {
        return aiChatThreadRepository.findByMbNoOrderByCreatedAtDesc(mbNo)
                .stream().map(this::toThreadDto).toList();
    }

    @Override
    public List<AiChatHistoryDto> getHistoriesByThread(Long threadId) {
        return aiChatHistoryRepository.findTop100ByThreadThreadIdOrderByCreatedAtDesc(threadId)
                .stream().map(this::toHistoryDto).toList();
    }

    @Override
    public List<AiChatHistoryDto> getHistoryDtoList(Long threadId) {
        return getHistoriesByThread(threadId);
    }

    @Override
    public AiChatHistoryDto saveValidatedMessage(AiChatHistorySaveRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }
        return saveHistory(request.getThreadId(), request.getRole(), request.getContent());
    }

    private AiChatThreadDto toThreadDto(AiChatThread thread) {
        return new AiChatThreadDto(thread.getThreadId(), thread.getMbNo(), thread.getTitle(), thread.getCreatedAt());
    }

    private AiChatHistoryDto toHistoryDto(AiChatHistory history) {
        return new AiChatHistoryDto(history.getHistoryId(), history.getRole(), history.getContent(), history.getCreatedAt());
    }

    private AiChatThread getThreadOrThrow(Long threadId) {
        return aiChatThreadRepository.findById(threadId)
                .orElseThrow(() -> new AiChatThreadNotFoundException("존재하지 않는 쓰레드입니다. threadId=" + threadId));
    }
}
