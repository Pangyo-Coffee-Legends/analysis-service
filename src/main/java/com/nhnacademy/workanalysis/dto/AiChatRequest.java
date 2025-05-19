package com.nhnacademy.workanalysis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiChatRequest {
    private Long memberNo;
    private List<Message> messages;

    @Getter @Setter
    public static class Message {
        private String role; // "user" or "model"
        private String text;
    }
}

