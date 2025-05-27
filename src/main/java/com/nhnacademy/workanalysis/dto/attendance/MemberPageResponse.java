package com.nhnacademy.workanalysis.dto.attendance;

import lombok.Data;

import java.util.List;

@Data
public class MemberPageResponse {
    private List<MemberInfoResponse> content;
    private int totalPages;
    private long totalElements;
    private int pageNumber;
}

