package com.nhnacademy.workanalysis.adaptor;

import com.nhnacademy.workanalysis.dto.attendance.MemberInfoResponse;
import com.nhnacademy.workanalysis.dto.attendance.MemberPageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "member-service", url = "${member.service.url}")
public interface MemberServiceClient {

    @GetMapping("/api/v1/members")
    MemberPageResponse getMemberInfoList(@RequestParam int page, @RequestParam int size);

    @GetMapping("/api/v1/members/{mbNo}/info")
    MemberInfoResponse getMemberByNo(@PathVariable("mbNo") Long mbNo);
}

