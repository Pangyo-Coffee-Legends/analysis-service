package com.nhnacademy.workanalysis.dto.attendance;

import lombok.Data;

import java.util.List;

/**
 * 회원 목록 조회 결과를 페이징 형태로 전달하기 위한 응답 DTO입니다.
 *
 * <p>회원 정보 리스트, 전체 페이지 수, 전체 항목 수, 현재 페이지 번호 등의 정보를 포함합니다.</p>
 */
@Data
public class MemberPageResponse {

    /**
     * 현재 페이지에 포함된 회원 정보 리스트
     */
    private List<MemberInfoResponse> content;

    /**
     * 전체 페이지 수
     */
    private int totalPages;

    /**
     * 전체 회원 수 (모든 페이지 기준)
     */
    private long totalElements;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int pageNumber;
}
