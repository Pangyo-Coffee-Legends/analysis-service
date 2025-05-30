package com.nhnacademy.workanalysis.config;

import com.nhnacademy.traceloggermodule.config.FeignTraceInterceptor;
import feign.RequestInterceptor;
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
        factory.setConnectTimeout(30000);  // 연결 시도 제한 (30초)
        factory.setReadTimeout(30000);     // 응답 대기 시간 제한 (30초)

        RestTemplate restTemplate = new RestTemplate(factory);

        log.debug("🔧 HttpComponentsClientHttpRequestFactory 설정 완료");
        log.info("✅ RestTemplate Bean 등록 완료");

        return restTemplate;
    }
    /**
     * Feign Client 요청에 대한 공통 인터셉터 빈을 등록합니다.
     * <p>
     * 이 인터셉터는 모든 Feign 요청에 대해 공통 헤더 또는 로깅 등의 처리를 할 수 있도록
     * {@link FeignTraceInterceptor}를 등록하며, 주로 트레이싱(trace), 인증 토큰 전달,
     * 로깅 등의 목적으로 사용됩니다.
     * </p>
     *
     * @return 모든 Feign 요청에 적용될 {@link RequestInterceptor} 구현체인 {@link FeignTraceInterceptor} 인스턴스
     */
    @Bean
    public RequestInterceptor feignTraceInterceptor() {
        return new FeignTraceInterceptor();
    }
}
