package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.dto.*;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;
import com.nhnacademy.workanalysis.exception.AiChatThreadNotFoundException;
import com.nhnacademy.workanalysis.repository.AiChatHistoryRepository;
import com.nhnacademy.workanalysis.repository.AiChatThreadRepository;
import com.nhnacademy.workanalysis.service.impl.AiChatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
    void testCreateThread() throws Exception {
        AiChatThread savedThread = AiChatThread.create(10L, "대화 제목");
        setField(savedThread, "threadId", 1L);

        when(aiChatThreadRepository.save(any())).thenReturn(savedThread);

        AiChatThreadDto result = aiChatService.createThread(10L, "대화 제목");

        assertThat(result.getThreadId()).isEqualTo(1L);
        assertThat(result.getMbNo()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("대화 제목");
    }

    @Test
    @DisplayName("updateThreadTitle: 제목 변경 성공")
    void testUpdateThreadTitle() {
        AiChatThread thread = AiChatThread.create(1L, "old");
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
                .isInstanceOf(AiChatThreadNotFoundException.class);
    }

    @Test
    @DisplayName("deleteThread: 존재하지 않을 경우 예외 발생")
    void testDeleteThread_NotFound() {
        when(aiChatThreadRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> aiChatService.deleteThread(999L))
                .isInstanceOf(AiChatThreadNotFoundException.class);
    }

    @Test
    @DisplayName("saveHistory: 히스토리 저장 성공")
    void testSaveHistory() throws Exception {
        AiChatThread thread = AiChatThread.create(1L, "질문");
        AiChatHistory history = AiChatHistory.of(thread, "user", "내용");
        setField(history, "historyId", 99L);

        when(aiChatThreadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(aiChatHistoryRepository.save(any())).thenReturn(history);

        AiChatHistoryDto result = aiChatService.saveHistory(1L, "user", "내용");

        assertThat(result.getHistoryId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("getThreadsByMember: 쓰레드 목록 반환")
    void testGetThreadsByMember() {
        when(aiChatThreadRepository.findByMbNoOrderByCreatedAtDesc(123L))
                .thenReturn(List.of(AiChatThread.create(123L, "제목")));

        List<AiChatThreadDto> result = aiChatService.getThreadsByMember(123L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getHistoriesByThread: 히스토리 목록 반환")
    void testGetHistoriesByThread() {
        AiChatThread thread = AiChatThread.create(1L, "세션");
        when(aiChatHistoryRepository.findTop100ByThreadThreadIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(AiChatHistory.of(thread, "user", "내용")));

        List<AiChatHistoryDto> result = aiChatService.getHistoriesByThread(1L);
        assertThat(result).hasSize(1);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
