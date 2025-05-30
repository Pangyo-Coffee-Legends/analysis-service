package com.nhnacademy.workanalysis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * PDF 리포트 생성 중 예외가 발생했을 때 던지는 커스텀 예외입니다.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PdfReportGenerationException extends RuntimeException {

    public PdfReportGenerationException(String message) {
        super(message);
    }

    public PdfReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
