package com.nhnacademy.workanalysis.exception;

/**
 * FeignClient 또는 프론트 전달 데이터에서
 * 근무 기록이 없을 경우 발생하는 도메인 예외
 */
public class WorkEntryRecordNotFoundException extends RuntimeException {
    public WorkEntryRecordNotFoundException(String message) {
        super("출결 데이터 없음");
    }

}
