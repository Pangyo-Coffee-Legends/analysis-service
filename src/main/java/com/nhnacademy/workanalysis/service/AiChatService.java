package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.entity.AiChatHistory;
import com.nhnacademy.workanalysis.entity.AiChatThread;

import java.util.List;

public interface AiChatService {
    GeminiAnalysisResponse analyze(GeminiAnalysisRequest request);

    AiChatThread createThread(Long mbNo,String title);

    AiChatHistory saveHistory(Long threadId, String role, String content);

    List<AiChatThread> getThreadsByMember(Long mbNo);

    List<AiChatHistory> getHistoriesByThread(Long threadId);

    void deleteThread(Long threadId);

    void updateThreadTitle(Long threadId, String newTitle);
}
