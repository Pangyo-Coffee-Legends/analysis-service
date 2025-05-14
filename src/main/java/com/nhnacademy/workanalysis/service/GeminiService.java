package com.nhnacademy.workanalysis.service;

import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;

public interface GeminiService {
    GeminiAnalysisResponse analyze(GeminiAnalysisRequest request);
}
