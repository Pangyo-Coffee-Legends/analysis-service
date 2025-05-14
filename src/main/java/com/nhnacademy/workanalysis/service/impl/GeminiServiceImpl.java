package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.adaptor.GeminiApiClient;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.util.PromptComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final GeminiApiClient geminiApiClient;

    @Override
    public GeminiAnalysisResponse analyze(GeminiAnalysisRequest request) {
        String prompt = PromptComposer.compose(request);
        return geminiApiClient.call(prompt, request.getMemberNo());
    }
}
