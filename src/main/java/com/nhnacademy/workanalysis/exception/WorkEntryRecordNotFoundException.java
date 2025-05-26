package com.nhnacademy.workanalysis.exception;

/**
 * FeignClient 또는 프론트 전달 데이터에서
 * 근무 기록이 없을 경우 발생하는 도메인 예외
 */
public class WorkEntryRecordNotFoundException extends RuntimeException {
    public WorkEntryRecordNotFoundException(Long mbNo, int year, int month) {
        super(String.format("사원 %d의 %d년 %d월 근무 기록이 존재하지 않습니다.", mbNo, year, month));
    }
}
