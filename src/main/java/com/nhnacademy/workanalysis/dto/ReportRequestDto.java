package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 사원별 리포트 생성을 위한 요청 DTO
 */
@Data
@AllArgsConstructor
public class ReportRequestDto {

    @NotNull(message = "사원 번호는 필수입니다.")
    private Long mbNo;

    @NotNull(message = "연도는 필수입니다.")
    private Integer year;

    @NotNull(message = "월은 필수입니다.")
    private Integer month;

    @NotEmpty(message = "상태 코드 리스트는 비어 있을 수 없습니다.")
    private List<String> statusCodes;
}
