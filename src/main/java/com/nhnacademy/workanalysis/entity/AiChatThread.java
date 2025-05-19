package com.nhnacademy.workanalysis.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * AI 분석 쓰레드 정보를 저장하는 JPA 엔티티 클래스입니다.
 * 사용자가 Gemini AI 분석을 요청한 단위(대화 흐름/세션)를 관리합니다.
 *
 * 하나의 쓰레드는 여러 개의 히스토리(AiChatHistory)로 구성되며,
 * 사원 번호(mbNo)와 쓰레드 제목(title), 생성 시간(created_at)을 포함합니다.
 *
 * 주요 컬럼:
 * - thread_id: 기본 키 (자동 생성)
 * - title: 사용자가 지정한 쓰레드 제목
 * - mb_no: 해당 쓰레드를 소유한 사원 번호
 * - created_at: 쓰레드 생성 시간
 * - histories: 쓰레드에 속한 전체 대화 메시지 목록 (OneToMany)
 */
@Entity
@Table(name = "ai_chat_thread")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AiChatThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thread_id")
    @JsonProperty("threadId")
    private Long threadId;

    @Column(name = "title")
    private String title;

    @Column(name = "mb_no")
    private Long mbNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * histories: AiChatHistory의 thread 필드에 의해 매핑됨
     */
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiChatHistory> histories = new ArrayList<>();
}
