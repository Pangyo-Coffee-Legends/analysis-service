package com.nhnacademy.workanalysis.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 전역 예외 처리 클래스입니다.
 * 커스텀 예외 및 공통 예외에 대한 응답을 처리합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ThreadNotFoundException 처리 핸들러입니다.
     *
     * @param ex ThreadNotFoundException 예외 객체
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(ThreadNotFoundException.class)
    public ResponseEntity<String> handleThreadNotFoundException(ThreadNotFoundException ex) {
        log.warn("ThreadNotFoundException 발생: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("쓰레드를 찾을 수 없습니다: " + ex.getMessage());
    }

    /**
     * IllegalArgumentException 처리 핸들러입니다.
     *
     * @param ex IllegalArgumentException 예외 객체
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException 발생: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("요청이 올바르지 않습니다: " + ex.getMessage());
    }

    /**
     * 그 외 모든 예외 처리 핸들러입니다.
     *
     * @param ex Exception 예외 객체
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        log.error("Unhandled Exception 발생", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
    }
}
