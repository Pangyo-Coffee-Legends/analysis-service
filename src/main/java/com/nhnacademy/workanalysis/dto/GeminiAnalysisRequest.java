package com.nhnacademy.workanalysis.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiAnalysisRequest {
     private Long memberNo;
     private String prompt;
     private List<WorkRecord> workRecords;

     @Getter
     @Setter
     public static class WorkRecord {
          private String date;
          private String dayOfWeek;
          private String statusCode;
          private String inTime;
          private String outTime;
     }
}
