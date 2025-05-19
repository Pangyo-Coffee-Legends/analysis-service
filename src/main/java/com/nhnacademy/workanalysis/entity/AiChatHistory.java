package com.nhnacademy.workanalysis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
/**
 * AI 대화 히스토리 정보를 저장하는 JPA 엔티티 클래스입니다.
 * 사용자가 Gemini API와 주고받은 대화 기록을 개별 메시지 단위로 저장합니다.
 *
 * 각 히스토리는 하나의 쓰레드(AiChatThread)에 소속되며,
 * 역할(예: 'user', 'ai'), 메시지 본문, 생성 시간 정보를 포함합니다.
 *
 * 주요 컬럼:
 * - history_id: 기본 키 (자동 생성)
 * - role: 메시지 작성자 역할 ('user' 또는 'ai')
 * - content: 메시지 본문 (Lob, 길이 제한 없음)
 * - created_at: 생성 시간
 * - thread_id: 소속 쓰레드 (ManyToOne 관계)
 */
@Entity
@Table(name = "ai_chat_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "role")
    private String role; // 'user' or 'ai'

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * thread: ai_chat_thread 테이블의 thread_id 컬럼과 매핑
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "thread_id", nullable = false)
    private AiChatThread thread;
}
