package com.nhnacademy.workanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiChatThreadTitleUpdateRequest {

    @NotNull
    private Long threadId;
    @NotBlank
    private String title;


}
