package com.nhnacademy.workanalysis.dto.attendance;

import lombok.Value;

/**
 * 회원의 상세 정보를 응답으로 전달하기 위한 DTO 클래스입니다.
 *
 * <p>회원 번호, 이름, 이메일, 전화번호, 역할명을 포함합니다.</p>
 */
@Value
public class MemberInfoResponse {

    /**
     * 회원 고유 번호 (PK)
     */
    Long no;

    /**
     * 회원 이름
     */
    String name;

    /**
     * 회원 이메일 주소
     */
    String email;

    /**
     * 회원 전화번호
     */
    String phoneNumber;

    /**
     * 회원 역할 이름 (예: ADMIN, USER 등)
     */
    String roleName;
}
