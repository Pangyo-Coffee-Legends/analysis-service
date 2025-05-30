package com.nhnacademy.workanalysis.exception;

/**
 * 쓰레드 제목이 비어 있을 경우 발생하는 예외입니다.
 */
public class ThreadTitleEmptyException extends RuntimeException {
    public ThreadTitleEmptyException(String message) {
        super(message);
    }
}
