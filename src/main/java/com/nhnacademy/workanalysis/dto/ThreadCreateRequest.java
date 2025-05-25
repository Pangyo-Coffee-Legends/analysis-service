package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 쓰레드 생성을 위한 요청 DTO입니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadCreateRequest {
    @NotNull
    private Long mbNo;
    @NotBlank
    private String title;
}
