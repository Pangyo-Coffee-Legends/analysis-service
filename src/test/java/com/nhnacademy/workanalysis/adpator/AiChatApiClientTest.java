package com.nhnacademy.workanalysis.adpator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.workanalysis.adaptor.AiChatApiClient;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisRequest;
import com.nhnacademy.workanalysis.dto.GeminiAnalysisResponse;
import com.nhnacademy.workanalysis.dto.MessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebMvcTest(AiChatApiClient.class)
class AiChatApiClientTest {

    @InjectMocks
    private AiChatApiClient aiChatApiClient;

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(aiChatApiClient, "apiKey", "fake-api-key");
        ReflectionTestUtils.setField(aiChatApiClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(aiChatApiClient, "objectMapper", objectMapper);
    }

    /**
     * Gemini API 호출 성공 시 응답 결과가 정상적으로 파싱되어 반환되는지 테스트합니다.
     */
    @Test
    void testCall_whenSuccess_shouldReturnParsedText() {
        // given
        MessageDto message = new MessageDto();
        message.setRole("user");
        message.setContent("출근 상태를 분석해줘");
        List<MessageDto> messages = List.of(message);

        String apiResponseJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      {
                        "text": "분석 결과입니다."
                      }
                    ]
                  }
                }
              ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        GeminiAnalysisResponse response = aiChatApiClient.call(messages, 1001L);

        // then
        assertThat(response.getMemberNo()).isEqualTo(1001L);
        assertThat(response.getFullText()).isEqualTo("분석 결과입니다.");
    }

    /**
     * Gemini API 호출 실패 시 예외 메시지가 포함된 응답이 반환되는지 테스트합니다.
     */
    @Test
    void testCall_whenApiFails_shouldReturnErrorMessage() {
        // given
        MessageDto message = new MessageDto();
        message.setRole("user");
        message.setContent("지각 상태 분석");
        List<MessageDto> messages = List.of(message);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // when
        GeminiAnalysisResponse response = aiChatApiClient.call(messages, 2002L);

        // then
        assertThat(response.getMemberNo()).isEqualTo(2002L);
        assertThat(response.getFullText()).contains("Gemini 호출 실패");
    }

    /**
     * Gemini 응답 JSON에서 텍스트가 누락된 경우 처리되는지 테스트합니다.
     */
    @Test
    void testCall_whenTextMissing_shouldReturnWarningMessage() {
        // given
        MessageDto message = new MessageDto();
        message.setRole("user");
        message.setContent("결근 상태 분석");
        List<MessageDto> messages = List.of(message);

        String invalidJson = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": []
                  }
                }
              ]
            }
            """;

        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(responseEntity);

        // when
        GeminiAnalysisResponse response = aiChatApiClient.call(messages, 3003L);

        // then
        assertThat(response.getMemberNo()).isEqualTo(3003L);
        assertThat(response.getFullText()).contains("파싱 실패");
    }
}
