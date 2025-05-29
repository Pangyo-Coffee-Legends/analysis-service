package com.nhnacademy.workanalysis.dto.attendance;

import lombok.Data;

import java.util.List;

/**
 * 공통 페이징 응답 DTO 클래스입니다.
 *
 * <p>제네릭 타입 T를 사용하여 다양한 목록 데이터에 재사용할 수 있으며,
 * 페이지 처리에 필요한 정보(전체 페이지 수, 총 항목 수, 페이지 크기, 현재 페이지 번호 등)를 포함합니다.</p>
 *
 * @param <T> 페이지 항목의 타입
 */
@Data
public class PageResponse<T> {

    /**
     * 현재 페이지에 포함된 항목 목록
     */
    private List<T> content;

    /**
     * 전체 페이지 수
     */
    private int totalPages;

    /**
     * 전체 항목 수
     */
    private long totalElements;

    /**
     * 페이지당 항목 수 (size)
     */
    private int size;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int number;
}
