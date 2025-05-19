package com.nhnacademy.workanalysis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정 클래스입니다.
 * 외부 HTTP API 통신을 위한 RestTemplate을 Bean으로 등록합니다.
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate 빈을 생성하고 등록합니다.
     * Spring의 의존성 주입을 통해 어디서든 사용할 수 있도록 합니다.
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("📡 RestTemplate Bean 생성 시작");

        RestTemplate restTemplate = new RestTemplate();

        log.debug("🔧 RestTemplate 기본 인스턴스 생성 완료");
        log.info("✅ RestTemplate Bean 등록 완료");

        return restTemplate;
    }
}
