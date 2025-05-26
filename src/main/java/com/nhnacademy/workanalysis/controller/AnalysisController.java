package com.nhnacademy.workanalysis.controller;

import com.nhnacademy.workanalysis.dto.*;
import com.nhnacademy.workanalysis.exception.ThreadTitleEmptyException;
import com.nhnacademy.workanalysis.exception.WorkEntryRecordNotFoundException;
import com.nhnacademy.workanalysis.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Gemini 기반의 AI 분석 및 대화 히스토리를 관리하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AiChatService aiChatService;

    /**
     * 사용자의 프롬프트 메시지를 기반으로 Gemini 분석을 요청합니다.
     *
     * @param request 분석 요청 메시지
     * @return 분석 결과
     */
    @PostMapping("/customs")
    public ResponseEntity<GeminiAnalysisResponse> analyzeWithPrompt(@RequestBody @Valid GeminiAnalysisRequest request) {
        log.info("🔍 [분석 요청] mbNo={}, message count={}", request.getMemberNo(), request.getMessages().size());
        GeminiAnalysisResponse result = aiChatService.analyze(request);
        log.info("✅ [분석 완료] 응답 길이={}자", result.getFullText().length());
        return ResponseEntity.ok(result);
    }

    /**
     * 새로운 쓰레드를 생성합니다.
     *
     * @param body 쓰레드 생성 요청 DTO (mbNo, title 포함)
     * @return 생성된 쓰레드 DTO
     */
    @PostMapping("/threads")
    public ResponseEntity<AiChatThreadDto> createThread(@RequestBody @Valid AiChatThreadCreateRequest body) {
        Long mbNo = body.getMbNo();
        String title = body.getTitle();

        log.info("📌 [쓰레드 생성] mbNo={}, title={}", mbNo, title);
        AiChatThreadDto thread = aiChatService.createThread(mbNo, title);
        log.debug("🧵 생성된 쓰레드 ID={}", thread.getThreadId());
        return ResponseEntity.ok(thread);
    }

    /**
     * 쓰레드의 제목을 수정합니다.
     *
     * @param id 쓰레드 ID
     * @param body JSON 요청 본문 (title)
     * @return 200 OK
     */
    @PutMapping("/threads/{id}")
    public ResponseEntity<Void> updateThread(@PathVariable Long id, @RequestBody @Valid AiChatThreadTitleUpdateRequest body) {
        String title = body.getTitle();
        if (title == null || title.trim().isEmpty()) {
            log.warn("{}번 쓰레드는 공백일 수 없습니다.",id);
            throw new ThreadTitleEmptyException("쓰레드 제목 없음");
        }
        log.info("✏️ [쓰레드 제목 수정] threadId={}, title={}", id, title);
        aiChatService.updateThreadTitle(id, title);
        return ResponseEntity.ok().build();
    }

    /**
     * 쓰레드를 삭제합니다.
     *
     * @param id 쓰레드 ID
     * @return 204 No Content
     */
    @DeleteMapping("/threads/{id}")
    public ResponseEntity<Void> deleteThread(@PathVariable Long id) {
        log.info("🗑️ [쓰레드 삭제 요청] threadId={}", id);
        aiChatService.deleteThread(id);
        log.info("🗑️ [쓰레드 삭제 완료] threadId={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 회원의 전체 쓰레드 목록을 조회합니다.
     *
     * @param mbNo 회원 번호
     * @return 쓰레드 목록
     */
    @GetMapping("/members/{mbNo}/threads")
    public ResponseEntity<List<AiChatThreadDto>> getThreads(@PathVariable Long mbNo) {
        log.info("📋 [쓰레드 목록 조회] mbNo={}", mbNo);
        List<AiChatThreadDto> threadList = aiChatService.getThreadsByMember(mbNo);
        log.debug("📦 조회된 쓰레드 수={}", threadList.size());
        return ResponseEntity.ok(threadList);
    }

    /**
     * 특정 쓰레드의 대화 히스토리 목록을 조회합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 대화 히스토리 목록 DTO
     */
    @GetMapping("/histories/{threadId}")
    public ResponseEntity<List<AiChatHistoryDto>> getHistories(@PathVariable Long threadId) {
        log.info("📜 [히스토리 조회] threadId={}", threadId);
        List<AiChatHistoryDto> dtoList = aiChatService.getHistoryDtoList(threadId);
        log.debug("📦 히스토리 개수={}", dtoList.size());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 새로운 대화 메시지를 저장합니다.
     *
     * @param request 저장할 대화 메시지 정보 (쓰레드 ID, 역할, 메시지 내용 포함)
     * @return 저장된 대화 히스토리 엔티티
     *
     * 요청 예시:
     * {
     *   "threadId": 1,
     *   "role": "user",
     *   "content": "오늘 근무 기록 알려줘"
     * }
     *
     * 응답 예시 (200 OK):
     * {
     *   "historyId": 99,
     *   "role": "user",
     *   "content": "오늘 근무 기록 알려줘",
     *   "createdAt": "2025-05-22T13:35:00"
     * }
     */
    @PostMapping("/histories")
    public ResponseEntity<AiChatHistoryDto> saveMessage(@RequestBody @Valid AiChatHistorySaveRequest request) {
        try {
            // 👉 content 값 실제 확인
            log.info("💬 [요청 파라미터] threadId={}, role={}, content={}",
                    request.getThreadId(), request.getRole(), request.getContent());

            AiChatHistoryDto saved = aiChatService.saveHistory(request.getThreadId(), request.getRole(), request.getContent());
            log.debug("💾 [대화 저장 완료] historyId={}", saved.getHistoryId());
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.error("❌ [대화 저장 실패] 유효성 검사 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("❌ [대화 저장 실패] 내부 예외 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/reports")
    public ResponseEntity<GeminiAnalysisResponse> generateAttendanceReport(@RequestBody @Valid ReportRequestDto request) {
        try {
            log.info("📝 [리포트 생성 요청] mbNo={}, year={}, month={}, codes={}",
                    request.getMbNo(), request.getYear(), request.getMonth(), request.getStatusCodes());

            GeminiAnalysisResponse response = aiChatService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (WorkEntryRecordNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

}
