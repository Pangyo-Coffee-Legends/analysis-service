package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.dto.MessageDto;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.exception.ThreadNotFoundException;
import com.nhnacademy.workanalysis.repository.AiChatHistoryRepository;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import com.nhnacademy.workanalysis.service.impl.AiChatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

    @Mock
    private AiChatApiClient aiChatApiClient;

    @Mock
    private AiChatThreadRepository aiChatThreadRepository;

    @Mock
    private AiChatHistoryRepository aiChatHistoryRepository;

    @InjectMocks
    private AiChatServiceImpl aiChatService;


    @Test
    @DisplayName("analyze: Gemini API 호출 성공")
    void testAnalyze() {
        GeminiAnalysisRequest request = new GeminiAnalysisRequest();
        request.setMemberNo(1L);
        request.setMessages(List.of(new MessageDto("user", "출근이 몇 시야?")));

        GeminiAnalysisResponse expected = new GeminiAnalysisResponse(1L, "응답");

        when(aiChatApiClient.call(any(), anyLong())).thenReturn(expected);

        GeminiAnalysisResponse result = aiChatService.analyze(request);

        assertThat(result.getFullText()).isEqualTo("응답");
        verify(aiChatApiClient, atLeastOnce()).call(any(), eq(1L));
    }

    @Test
    @DisplayName("createThread: 쓰레드 생성")
    void testCreateThread() {
        AiChatThread savedThread = new AiChatThread();
        savedThread.setThreadId(1L);
        savedThread.setMbNo(10L);
        savedThread.setTitle("대화 제목");

        when(aiChatThreadRepository.save(any())).thenReturn(savedThread);

        AiChatThread result = aiChatService.createThread(10L, "대화 제목");

        assertThat(result.getThreadId()).isEqualTo(1L);
        assertThat(result.getMbNo()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("대화 제목");
    }

    @Test
    @DisplayName("updateThreadTitle: 제목 변경 성공")
    void testUpdateThreadTitle() {
        AiChatThread thread = new AiChatThread();
        thread.setThreadId(1L);
        thread.setTitle("old");

        when(aiChatThreadRepository.findById(1L)).thenReturn(Optional.of(thread));

        aiChatService.updateThreadTitle(1L, "new title");

        assertThat(thread.getTitle()).isEqualTo("new title");
        verify(aiChatThreadRepository).save(thread);
    }

    @Test
    @DisplayName("updateThreadTitle: 존재하지 않는 쓰레드")
    void testUpdateThreadTitleNotFound() {
        when(aiChatThreadRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiChatService.updateThreadTitle(999L, "any"))
                .isInstanceOf(ThreadNotFoundException.class);
    }

    @Test
    @DisplayName("deleteThread: 존재하지 않을 경우 예외 발생")
    void testDeleteThread_NotFound() {
        when(aiChatThreadRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> aiChatService.deleteThread(999L))
                .isInstanceOf(ThreadNotFoundException.class);
    }

    @Test
    @DisplayName("saveHistory: 히스토리 저장 성공")
    void testSaveHistory() {
        AiChatThread thread = new AiChatThread();
        thread.setThreadId(1L);

        AiChatHistory history = new AiChatHistory();
        history.setHistoryId(99L);

        when(aiChatThreadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(aiChatHistoryRepository.save(any())).thenReturn(history);

        AiChatHistory result = aiChatService.saveHistory(1L, "user", "내용");

        assertThat(result.getHistoryId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("getThreadsByMember: 쓰레드 목록 반환")
    void testGetThreadsByMember() {
        when(aiChatThreadRepository.findByMbNoOrderByCreatedAtDesc(123L))
                .thenReturn(List.of(new AiChatThread()));

        List<AiChatThread> result = aiChatService.getThreadsByMember(123L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getHistoriesByThread: 히스토리 목록 반환")
    void testGetHistoriesByThread() {
        when(aiChatHistoryRepository.findByThread_ThreadIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(new AiChatHistory()));

        List<AiChatHistory> result = aiChatService.getHistoriesByThread(1L);
        assertThat(result).hasSize(1);
    }
}
