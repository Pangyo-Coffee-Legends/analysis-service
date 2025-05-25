package com.nhnacademy.workanalysis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스입니다.
 * Spring Boot 3.4 이상에서는 HttpComponentsClientHttpRequestFactory로 타임아웃을 설정합니다.
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * 커넥션 타임아웃 및 읽기 타임아웃을 포함한 RestTemplate 빈 등록
     *
     * @return 설정된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("📡 RestTemplate Bean 생성 시작 (타임아웃 설정 포함)");

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000);  // 연결 시도 제한 (3초)
        factory.setReadTimeout(5000);     // 응답 대기 시간 제한 (5초)

        RestTemplate restTemplate = new RestTemplate(factory);

        log.debug("🔧 HttpComponentsClientHttpRequestFactory 설정 완료");
        log.info("✅ RestTemplate Bean 등록 완료");

        return restTemplate;
    }
}
