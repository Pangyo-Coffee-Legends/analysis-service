package com.nhnacademy.workanalysis.adpator;

import com.nhnacademy.workanalysis.adaptor.WorkEntryClient;
import com.nhnacademy.workanalysis.config.FeignTestConfig;
import com.nhnacademy.workanalysis.dto.attendance.AttendanceSummaryDto;
import com.nhnacademy.workanalysis.dto.attendance.PageResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link WorkEntryClient}의 최근 30일 출결 요약 데이터를 조회하는 Feign 호출을 테스트하는 클래스입니다.
 *
 * <p>MockWebServer를 사용하여 실제 백엔드 호출 없이 응답을 시뮬레이션합니다.</p>
 */
@SpringBootTest
@TestPropertySource(properties = {
        "work.entry.service.url=http://localhost:8082"
})
@Import(FeignTestConfig.class)
class WorkEntryClientTest {

    static MockWebServer mockWebServer;

    @Autowired
    WorkEntryClient workEntryClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8082);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * 특정 사원의 최근 30일 근태 요약 데이터를 정상적으로 조회하는지 검증합니다.
     */
    @Test
    @DisplayName("출결 요약 조회 - LocalDateTime 형식 inTime/outTime 파싱 검증")
    void testGetRecent30DaySummary() {
        String responseJson = """
        {
          "content": [
            {
              "year": 2025,
              "monthValue": 6,
              "dayOfMonth": 1,
              "hoursWorked": 8,
              "inTime": "2025-06-01T09:00:00",
              "outTime": "2025-06-01T18:00:00",
              "code": 1
            }
          ],
          "totalElements": 1,
          "totalPages": 1
        }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        PageResponse<AttendanceSummaryDto> response = workEntryClient.getRecent30DaySummary(101L);
        List<AttendanceSummaryDto> content = response.getContent();

        assertThat(content).hasSize(1);
        assertThat(content.getFirst().getCode()).isEqualTo(1L);
        assertThat(content.getFirst().getInTime().toString()).isEqualTo("2025-06-01T09:00");
    }

}
