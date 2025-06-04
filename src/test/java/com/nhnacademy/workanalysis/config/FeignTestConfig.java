package com.nhnacademy.workanalysis.config;

import okhttp3.mockwebserver.MockWebServer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Feign 클라이언트 테스트를 위한 전용 설정 클래스입니다.
 *
 * <p>
 * 이 설정은 테스트 환경에서 FeignClient가 정상적으로 동작하도록 필요한 자동 구성과 메시지 컨버터를 제공합니다.
 * 주로 {@link MockWebServer}를 사용하는 단위 테스트에서 Feign 클라이언트의 직렬화 및 역직렬화 처리를 지원하기 위해 사용됩니다.
 * </p>
 *
 * <p>
 * 주요 기능:
 * <ul>
 *   <li>{@link FeignAutoConfiguration}을 자동으로 임포트하여 Feign 관련 설정 활성화</li>
 *   <li>{@link MappingJackson2HttpMessageConverter}를 수동 등록하여 JSON 변환 문제 해결</li>
 * </ul>
 * </p>
 *
 * <p>
 * 이 설정 클래스는 `@SpringBootTest` 환경에서 테스트 시 함께 `@Import(FeignTestConfig.class)`로 사용됩니다.
 * </p>
 */
@TestConfiguration
@ImportAutoConfiguration(FeignAutoConfiguration.class)
public class FeignTestConfig {

    /**
     * JSON 메시지 변환기 빈을 수동 등록합니다.
     *
     * <p>
     * FeignClient가 응답을 {@code JSON → DTO}로 변환할 때 사용되며,
     * 기본 컨버터가 누락되었거나 커스터마이징이 필요한 테스트 환경에서
     * 명시적으로 등록함으로써 직렬화/역직렬화 오류를 방지합니다.
     * </p>
     *
     * @return Jackson 기반의 HTTP 메시지 변환기 인스턴스
     */
    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }
}
