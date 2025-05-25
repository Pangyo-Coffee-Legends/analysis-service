package com.nhnacademy.workanalysis.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.support.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 전역 예외 처리 클래스입니다.
 * 커스텀 예외 및 공통 예외에 대한 응답을 처리합니다.
 */
@RestControllerAdvice
@Slf4j
public class GlobalAdviceHandler {

    /**
     * ThreadNotFoundException 처리 핸들러입니다.
     *
     * @param ex ThreadNotFoundException 예외 객체
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(AiChatThreadNotFoundException.class)
    public ResponseEntity<String> handleThreadNotFoundException(AiChatThreadNotFoundException ex) {
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
     * TextNotFoundException 처리 핸들러입니다.
     * Gemini API 응답 JSON 내에 분석 결과 텍스트가 존재하지 않을 경우 발생합니다.
     *
     * 사용된 상태 코드 설명
     * HttpStatus.UNPROCESSABLE_ENTITY (422)는 요청은 문법적으로 올바르나,
     * 서버가 의미 있는 응답을 생성할 수 없는 경우에 사용됩니다. 분석 결과가 JSON에는 있으나 필요한 필드가 없을 때 적절합니다.
     *
     * @see <a href="https://developer.mozilla.org/ko/docs/Web/HTTP/Status/422">...</a>
     * @param ex TextNotFoundException 예외 객체
     * @return 422 Unprocessable Entity 응답
     */
    @ExceptionHandler(TextNotFoundException.class)
    public ResponseEntity<String> handleTextNotFoundException(TextNotFoundException ex) {
        log.warn("TextNotFoundException 발생: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body("분석 결과 텍스트를 찾을 수 없습니다: " + ex.getMessage());
    }
    /**
     * 유효성 검사(@Valid) 실패 시 발생하는 예외 처리기입니다.
     *
     * @param ex MethodArgumentNotValidException 예외
     * @return 필드별 오류 메시지를 포함한 400 Bad Request 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("❌ 유효성 검사 실패: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "요청 필드에 오류가 있습니다.");

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : Objects.requireNonNull(ex.getBindingResult()).getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        response.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 제약 조건 위반(@Validated, @NotNull 등) 발생 시 예외를 처리합니다.
     *
     * @param ex ConstraintViolationException 예외
     * @return 위반된 제약 조건 메시지들을 포함한 400 Bad Request 응답
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("❌ 제약 조건 위반: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "입력 값 제약 조건을 위반했습니다.");

        List<String> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        response.put("errors", violations);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * JSON 파싱 오류 또는 요청 본문의 구조적 문제가 발생할 때 처리합니다.
     *
     * @param ex HttpMessageNotReadableException 예외
     * @return JSON 형식 오류에 대한 400 Bad Request 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("❌ JSON 파싱 오류: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "요청 본문의 JSON 형식이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 매개변수 타입 불일치(예: 숫자 자리에 문자열 입력) 시 예외를 처리합니다.
     *
     * @param ex MethodArgumentTypeMismatchException 예외
     * @return 타입 오류 메시지를 포함한 400 Bad Request 응답
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("❌ 매개변수 타입 불일치: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "요청 파라미터 타입이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 지원하지 않는 Content-Type으로 요청이 들어온 경우 예외를 처리합니다.
     *
     * @param ex HttpMediaTypeNotSupportedException 예외 객체
     * @return 415 Unsupported Media Type 상태 코드와 설명 메시지를 포함한 ResponseEntity
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<String> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("지원하지 않는 Content-Type입니다.");
    }
    /**
     * 그 외 모든 예외 처리 핸들러입니다.
     *
     * @param ex Throwable 예외 객체
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleGeneralException(Throwable ex) {
        log.error("Unhandled Exception 발생", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");
    }



}
