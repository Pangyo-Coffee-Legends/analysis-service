package com.nhnacademy.workanalysis.dto.attendance;

import lombok.Value;

@Value
public class MemberInfoResponse {

    Long no;
    String name;
    String email;
    String phoneNumber;
    String roleName;
}