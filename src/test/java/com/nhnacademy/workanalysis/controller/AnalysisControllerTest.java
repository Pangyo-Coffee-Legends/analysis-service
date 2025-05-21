package com.nhnacademy.workanalysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.dto.MessageDto;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.service.AiChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalysisControllerTest {
    @Mock
    private MockMvc mockMvc;
    @Mock
    private AiChatService aiChatService;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        aiChatService = Mockito.mock(AiChatService.class);
        AnalysisController controller = new AnalysisController(aiChatService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /custom - 분석 요청 성공")
    void analyzeWithPrompt_success() throws Exception {
        GeminiAnalysisRequest request = new GeminiAnalysisRequest();
        request.setMemberNo(1L);
        request.setMessages(List.of(new MessageDto("user", "근무시간 알려줘")));

        GeminiAnalysisResponse response = new GeminiAnalysisResponse(1L, "분석 결과입니다.");

        Mockito.when(aiChatService.analyze(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/analysis/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullText").value("분석 결과입니다."));
    }

    @Test
    @DisplayName("POST /thread - 쓰레드 생성")
    void createThread_success() throws Exception {
        AiChatThread thread = new AiChatThread();
        thread.setThreadId(10L);
        thread.setMbNo(1L);
        thread.setTitle("새로운 쓰레드");

        Mockito.when(aiChatService.createThread(1L, "새로운 쓰레드")).thenReturn(thread);

        mockMvc.perform(post("/api/v1/analysis/thread")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("mbNo", 1L, "title", "새로운 쓰레드"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threadId").value(10L));
    }

    @Test
    @DisplayName("PATCH /thread/{id} - 쓰레드 제목 수정")
    void updateThreadTitle_success() throws Exception {
        mockMvc.perform(patch("/api/v1/analysis/thread/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "수정된 제목"))))
                .andExpect(status().isOk());

        Mockito.verify(aiChatService).updateThreadTitle(10L, "수정된 제목");
    }

    @Test
    @DisplayName("GET /thread/{mbNo} - 쓰레드 목록 조회")
    void getThreads_success() throws Exception {
        AiChatThread thread = new AiChatThread();
        thread.setThreadId(1L);
        thread.setMbNo(123L);
        thread.setTitle("테스트");

        Mockito.when(aiChatService.getThreadsByMember(123L)).thenReturn(List.of(thread));

        mockMvc.perform(get("/api/v1/analysis/thread/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("테스트"));
    }

    @Test
    @DisplayName("GET /history/{threadId} - 히스토리 조회")
    void getHistories_success() throws Exception {
        AiChatThread aiChatThread = new AiChatThread();
        AiChatHistory history = new AiChatHistory(10L, "user", "질문입니다", LocalDateTime.now(), aiChatThread);
        Mockito.when(aiChatService.getHistoriesByThread(10L)).thenReturn(List.of(history));

        mockMvc.perform(get("/api/v1/analysis/history/10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /history/save - 대화 저장 성공")
    void saveMessage_success() throws Exception {
        AiChatHistory history = new AiChatHistory();
        history.setHistoryId(99L);

        Mockito.when(aiChatService.saveHistory(10L, "user", "내용")).thenReturn(history);

        mockMvc.perform(post("/api/v1/analysis/history/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "threadId", 10,
                                "role", "user",
                                "content", "내용"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historyId").value(99));
    }

    @Test
    @DisplayName("DELETE /thread/{id} - 쓰레드 삭제")
    void deleteThread_success() throws Exception {
        mockMvc.perform(delete("/api/v1/analysis/thread/5"))
                .andExpect(status().isNoContent());

        Mockito.verify(aiChatService).deleteThread(5L);
    }
}
