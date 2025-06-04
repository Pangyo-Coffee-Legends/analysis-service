package com.nhnacademy.workanalysis.exception;

/**
 * GEN AI가 분석한 자료를 front로 JSON형식으로 전송하는 텍스트를 찾을 수 없을때 발생하는 exception
 */
public class TextNotFoundException extends RuntimeException {
    public TextNotFoundException(String message) {
        super(message);
    }
}
