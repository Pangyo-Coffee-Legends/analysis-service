package com.nhnacademy.workanalysis.adaptor;

import com.nhnacademy.workanalysis.dto.attendance.MemberInfoResponse;
import com.nhnacademy.workanalysis.dto.attendance.MemberPageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 회원 정보를 조회하기 위해 member-service와 통신하는 Feign Client 인터페이스입니다.
 *
 * <p>기능:</p>
 * <ul>
 *     <li>전체 회원 목록 조회 (페이징)</li>
 *     <li>특정 회원 번호로 회원 상세 정보 조회</li>
 * </ul>
 */
@FeignClient(name = "member-service", url = "${member.service.url}")
public interface MemberServiceClient {

    /**
     * 전체 회원 목록을 페이징하여 조회합니다.
     *
     * @param page 조회할 페이지 번호 (0부터 시작)
     * @param size 한 페이지당 회원 수
     * @return 회원 목록 및 페이징 정보가 포함된 {@link MemberPageResponse}
     */
    @GetMapping("/api/v1/members")
    MemberPageResponse getMemberInfoList(@RequestParam int page, @RequestParam int size);

    /**
     * 회원 고유 번호(mbNo)를 기반으로 해당 회원의 상세 정보를 조회합니다.
     *
     * @param mbNo 회원 번호
     * @return 회원 상세 정보가 포함된 {@link MemberInfoResponse}
     */
    @GetMapping("/api/v1/members/{mbNo}/info")
    MemberInfoResponse getMemberByNo(@PathVariable("mbNo") Long mbNo);
}
