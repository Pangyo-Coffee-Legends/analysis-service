package com.nhnacademy.workanalysis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Gemini API에 전달되는 분석 요청 DTO 클래스입니다.
 * 사원 번호, 멀티턴 대화 메시지, 근태 기록 데이터를 포함합니다.
 */
@Getter
@Setter
public class GeminiAnalysisRequest {

     /**
      * 분석 대상 사원의 고유 번호입니다.
      */
     private Long memberNo;

     /**
      * 사용자가 AI에게 보낸 질문들과 그에 대한 응답들을 포함하는 대화 이력입니다.
      */
     private List<Message> messages;

     /**
      * 분석에 참고할 근태 기록 리스트입니다.
      * 일자별 출결 상태, 시간 등이 포함됩니다.
      */
     private List<WorkRecord> workRecords;

     /**
      * 근태 기록 정보를 표현하는 내부 정적 클래스입니다.
      * 프롬프트 생성 시 템플릿으로 사용됩니다.
      */
     @Getter
     @Setter
     public static class WorkRecord {
          /**
           * 근무 일자 (예: "2025-05-19")
           */
          private String date;

          /**
           * 요일 정보 (예: "월", "화")
           */
          private String dayOfWeek;

          /**
           * 근태 상태 코드 (예: "1": 출근, "2": 지각 등)
           */
          private String statusCode;

          /**
           * 출근 시간 (예: "09:00")
           */
          private String inTime;

          /**
           * 퇴근 시간 (예: "18:00")
           */
          private String outTime;
     }

     /**
      * 대화 메시지를 표현하는 내부 정적 클래스입니다.
      * Gemini의 멀티턴 형식에 맞춰 role과 content 구조를 가집니다.
      */
     @Getter
     @Setter
     @NoArgsConstructor
     public static class Message {
          /**
           * 메시지 작성자 역할입니다.
           * 예: "user", "model"
           */
          private String role;

          /**
           * 메시지 본문 텍스트입니다.
           */
          private String content;
     }
}
