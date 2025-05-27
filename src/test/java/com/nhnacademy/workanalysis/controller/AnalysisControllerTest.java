package com.nhnacademy.workanalysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.workanalysis.dto.*;
import com.nhnacademy.workanalysis.service.AiChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiChatService aiChatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /customs - Gemini 분석 요청")
    void analyzeWithPrompt_success() throws Exception {
        GeminiAnalysisRequest request = new GeminiAnalysisRequest(1L, List.of(new MessageDto("user", "질문")), null);
        GeminiAnalysisResponse response = new GeminiAnalysisResponse(1L, "분석 결과입니다.");

        Mockito.when(aiChatService.analyze(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/analysis/customs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullText").value("분석 결과입니다."));
    }

    @Test
    @DisplayName("POST /customs - Content-Type 누락 시 415 응답")
    void analyzeWithPrompt_unsupportedMediaType() throws Exception {
        GeminiAnalysisRequest request = new GeminiAnalysisRequest(1L, List.of(new MessageDto("user", "질문")), null);

        mockMvc.perform(post("/api/v1/analysis/customs")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("POST /customs - 필드 누락 시 400 응답")
    void analyzeWithPrompt_missingField() throws Exception {
        String invalidJson = "{\"messages\": [{\"role\": \"user\", \"content\": \"테스트\"}]}"; // memberNo 누락

        mockMvc.perform(post("/api/v1/analysis/customs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("memberNo는 필수입니다.")));
    }

    @Test
    @DisplayName("POST /threads - 쓰레드 생성")
    void createThread_success() throws Exception {
        AiChatThreadDto threadDto = new AiChatThreadDto(10L, 1L, "테스트 쓰레드", LocalDateTime.now());
        AiChatThreadCreateRequest req = new AiChatThreadCreateRequest(1L, "테스트 쓰레드");

        Mockito.when(aiChatService.createThread(1L, "테스트 쓰레드")).thenReturn(threadDto);

        mockMvc.perform(post("/api/v1/analysis/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadId").value(10L));
    }

    @Test
    @DisplayName("PUT /threads/{id} - 쓰레드 제목 수정")
    void updateThread_success() throws Exception {
        mockMvc.perform(put("/api/v1/analysis/threads/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "수정된 제목"))))
                .andExpect(status().isOk());

        Mockito.verify(aiChatService).updateThreadTitle(10L, "수정된 제목");
    }

    @Test
    @DisplayName("GET /members/{mbNo}/threads - 쓰레드 목록 조회")
    void getThreads_success() throws Exception {
        AiChatThreadDto thread = new AiChatThreadDto(1L, 123L, "제목", LocalDateTime.now());
        Mockito.when(aiChatService.getThreadsByMember(123L)).thenReturn(List.of(thread));

        mockMvc.perform(get("/api/v1/analysis/members/123/threads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("제목"));
    }

    @Test
    @DisplayName("GET /histories/{threadId} - 히스토리 조회 + 구조 검증")
    void getHistories_success() throws Exception {
        AiChatHistoryDto dto = new AiChatHistoryDto(55L, "user", "내용", LocalDateTime.of(2025, 5, 23, 10, 0));

        Mockito.when(aiChatService.getHistoryDtoList(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/analysis/histories/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].historyId").value(55L))
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[0].content").value("내용"))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    @DisplayName("POST /histories/save - 대화 저장")
    void saveMessage_success() throws Exception {
        AiChatHistoryDto saved = new AiChatHistoryDto(99L, "user", "내용", LocalDateTime.now());
        AiChatHistorySaveRequest req = new AiChatHistorySaveRequest(10L, "user", "내용");

        Mockito.when(aiChatService.saveHistory(10L, "user", "내용")).thenReturn(saved);

        mockMvc.perform(post("/api/v1/analysis/histories/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historyId").value(99));
    }

    @Test
    @DisplayName("POST /histories/save - 필드 누락 시 400")
    void saveMessage_missingField_shouldReturn400() throws Exception {
        String json = "{\"threadId\": 10, \"role\": \"user\"}"; // content 누락

        mockMvc.perform(post("/api/v1/analysis/histories/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /threads/{id} - 쓰레드 삭제")
    void deleteThread_success() throws Exception {
        mockMvc.perform(delete("/api/v1/analysis/threads/99"))
                .andExpect(status().isNoContent());

        Mockito.verify(aiChatService).deleteThread(99L);
    }
}
