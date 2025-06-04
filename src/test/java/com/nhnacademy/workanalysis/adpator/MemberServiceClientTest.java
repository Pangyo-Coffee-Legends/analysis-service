package com.nhnacademy.workanalysis.adpator;

import com.nhnacademy.workanalysis.adaptor.MemberServiceClient;
import com.nhnacademy.workanalysis.config.FeignTestConfig;
import com.nhnacademy.workanalysis.dto.attendance.MemberInfoResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link MemberServiceClient}의 Feign 호출을 테스트하기 위한 통합 테스트 클래스입니다.
 *
 * <p>MockWebServer를 사용하여 실제 HTTP 서버 없이 응답을 시뮬레이션합니다.</p>
 */
@SpringBootTest
@TestPropertySource(properties = {
        "work.entry.service.url=http://localhost:8082",
        "member.service.url=http://localhost:8081",
        "gemini.api.key=dummy-key-for-test"
})
@Import(FeignTestConfig.class) // JSON 직렬화/역직렬화 관련 설정 분리
class MemberServiceClientTest {

    static MockWebServer mockWebServer;

    @Autowired
    MemberServiceClient memberServiceClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8081);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    /**
     * 회원 번호로 회원 상세 정보를 조회하는 기능을 검증합니다.
     */
    @Test
    @DisplayName("회원 상세 조회 - mbNo로 이름 및 이메일 검증")
    void testGetMemberByNo() {
        String responseJson = """
            {
              "mbNo": 101,
              "name": "홍길동",
              "email": "hong@example.com",
              "phonenumber": "010-1234-5678"
            }
            """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        MemberInfoResponse result = memberServiceClient.getMemberByNo(101L, "detailed");

        assertThat(result.getMbNo()).isEqualTo(101L);
        assertThat(result.getName()).isEqualTo("홍길동");
    }
}
