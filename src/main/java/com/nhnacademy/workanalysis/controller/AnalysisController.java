package com.nhnacademy.workanalysis.controller;

import com.nhnacademy.workanalysis.dto.AiChatHistoryDto;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @PostMapping("/custom")
    public ResponseEntity<GeminiAnalysisResponse> analyzeWithPrompt(@RequestBody GeminiAnalysisRequest request) {
        log.info("🔍 [분석 요청] mbNo={}, message count={}", request.getMemberNo(), request.getMessages().size());
        GeminiAnalysisResponse result = aiChatService.analyze(request);
        log.info("✅ [분석 완료] 응답 길이={}자", result.getFullText().length());
        return ResponseEntity.ok(result);
    }

    /**
     * 새로운 쓰레드를 생성합니다.
     *
     * @param body JSON 요청 본문 (mbNo, title)
     * @return 생성된 쓰레드 정보
     */
    @PostMapping("/thread")
    public ResponseEntity<AiChatThread> createThread(@RequestBody Map<String, Object> body) {
        Long mbNo = Long.parseLong(body.get("mbNo").toString());
        String title = body.get("title").toString();
        log.info("📌 [쓰레드 생성] mbNo={}, title={}", mbNo, title);
        AiChatThread thread = aiChatService.createThread(mbNo, title);
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
    @PatchMapping("/thread/{id}")
    public ResponseEntity<Void> updateThread(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title = body.get("title").toString();
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
    @DeleteMapping("/thread/{id}")
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
    @GetMapping("/thread/{mbNo}")
    public ResponseEntity<List<AiChatThread>> getThreads(@PathVariable Long mbNo) {
        log.info("📋 [쓰레드 목록 조회] mbNo={}", mbNo);
        List<AiChatThread> threadList = aiChatService.getThreadsByMember(mbNo);
        log.debug("📦 조회된 쓰레드 수={}", threadList.size());
        return ResponseEntity.ok(threadList);
    }

    /**
     * 특정 쓰레드의 대화 히스토리 목록을 조회합니다.
     *
     * @param threadId 쓰레드 ID
     * @return 대화 히스토리 목록 DTO
     */
    @GetMapping("/history/{threadId}")
    public ResponseEntity<List<AiChatHistoryDto>> getHistories(@PathVariable Long threadId) {
        log.info("📜 [히스토리 조회] threadId={}", threadId);
        List<AiChatHistoryDto> dtoList = aiChatService.getHistoriesByThread(threadId)
                .stream()
                .map(h -> new AiChatHistoryDto(h.getRole(), h.getContent(), h.getCreatedAt()))
                .toList();
        log.debug("📦 히스토리 개수={}", dtoList.size());
        return ResponseEntity.ok(dtoList);
    }

    /**
     * 새로운 대화 메시지를 저장합니다.
     *
     * @param body JSON 요청 본문 (threadId, role, content)
     * @return 저장된 대화 히스토리
     */
    @PostMapping("/history/save")
    public ResponseEntity<AiChatHistory> saveMessage(@RequestBody Map<String, Object> body) {
        Object threadIdRaw = body.get("threadId");
        Object roleRaw = body.get("role");
        Object contentRaw = body.get("content");

        if (threadIdRaw == null || roleRaw == null || contentRaw == null) {
            log.error("❌ [대화 저장 실패] 필드 누락 - threadId={}, role={}, content={}",
                    threadIdRaw, roleRaw, contentRaw);
            return ResponseEntity.badRequest().build();
        }

        try {
            Long threadId = Long.parseLong(threadIdRaw.toString());
            String role = roleRaw.toString();
            String content = contentRaw.toString();

            log.info("📝 [대화 저장 요청] threadId={}, role={}, content length={}", threadId, role, content.length());
            AiChatHistory saved = aiChatService.saveHistory(threadId, role, content);
            log.debug("💾 [대화 저장 완료] historyId={}", saved.getHistoryId());
            return ResponseEntity.ok(saved);

        } catch (NumberFormatException e) {
            log.error("❌ [대화 저장 실패] threadId 파싱 오류: {}", threadIdRaw, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ [대화 저장 실패] 내부 예외 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
