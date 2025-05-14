package com.nhnacademy.workanalysis.controller;

import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final GeminiService geminiService;

    @PostMapping("/custom")
    public ResponseEntity<GeminiAnalysisResponse> analyzeWithPrompt(@RequestBody GeminiAnalysisRequest request) {
        return ResponseEntity.ok(geminiService.analyze(request));
    }
}
