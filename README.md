# 📌 Gemini 기반 AI 근태 분석 및 PDF 리포트 시스템

근무 데이터를 Gemini API로 분석하고, 분석 결과를 시각화(PDF 포함)하여  
인사관리 및 근태 모니터링에 효율적으로 활용할 수 있도록 만든 AI 분석 시스템입니다.

---

## ✍ 작성자 정보
- 작성자: @Migong0311

- 소속: 판교 스타벅스에서 봅시다 팀(AIoT2-2팀)
- 작성일: 2025-06-04
- 상태: ✅ 개발 완료

---

## 🛠 기술 스택 및 개발 환경

### 🔧 Backend / Frontend / Infra

![Java](https://img.shields.io/badge/Java-007396?style=flat&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat&logo=javascript&logoColor=black)
![JSON](https://img.shields.io/badge/JSON-000000?style=flat&logo=json&logoColor=white)

### 💻 IDE & Tools

![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000?style=flat&logo=intellij-idea&logoColor=white)

### 🖥 OS 환경

![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=flat&logo=ubuntu&logoColor=white)


| 영역         | 기술                                      |
|------------|-----------------------------------------|
| **분석 API** | Google Gemini 2.0 Flash                 |
| **시각화**    | Chart.js, marked.js                     |
| **PDF 생성** | iText 2.1.7 (***BufferedImage*** 기반 차트) |

---

## 🗂 프로젝트 구조

```bash
📦 src
 ┣ 📂 main
 ┃ ┣ 📂 java
 ┃ ┃ ┗ 📂 com.nhnacademy.workanalysis
 ┃ ┃ ┃ ┣ 📂 adaptor
 ┃ ┃ ┃ ┣ 📂 config
 ┃ ┃ ┃ ┣ 📂 controller
 ┃ ┃ ┃ ┣ 📂 dto
 ┃ ┃ ┃ ┣ 📂 entity
 ┃ ┃ ┃ ┣ 📂 exception
 ┃ ┃ ┃ ┣ 📂 generator
 ┃ ┃ ┃ ┣ 📂 repository
 ┃ ┃ ┃ ┗ 📂 service
 ┃ ┗ 📂 resources
 ┣ 📂 test
```

---

## 🎯 주요 기능
### ✅ Gemini AI 기반 근태 분석
- 사원의 근태 기록 기반으로 프롬프트에 따라 AI 분석 수행
- 정규식 기반 패턴 인식 후, 시각화(Chart.js) 자동 출력

### ✅ 대화 쓰레드 / 히스토리 관리
- 각 사원별 쓰레드 목록(대화 기록)CRUD 가능 (생성, 수정, 삭제, 조회)
- 히스토리(대화내역) 사원 별 저장 기능

### ✅ 리포트 PDF 자동 생성
- 사원, 연도, 월 선택 후 한 번의 클릭으로 리포트 PDF 다운로드
- 리포트 구성: 요약표 + 바 차트 + 도넛 차트

---

## 📡 주요 API 엔드포인트
| 메서드    | URI                                       | 설명                   |
| ------ | ----------------------------------------- | -------------------- |
| POST   | `/api/v1/analysis/customs`                | Gemini AI 분석 요청      |
| POST   | `/api/v1/analysis/threads`                | 쓰레드 생성               |
| PUT    | `/api/v1/analysis/threads/{id}`           | 쓰레드 제목 수정            |
| DELETE | `/api/v1/analysis/threads/{id}`           | 쓰레드 삭제               |
| GET    | `/api/v1/analysis/members/{mbNo}/threads` | 사원별 쓰레드 목록 조회        |
| GET    | `/api/v1/analysis/histories/{threadId}`   | 쓰레드 대화 내역 조회         |
| POST   | `/api/v1/analysis/histories`              | 대화 메시지 저장            |
| POST   | `/api/v1/analysis/reports`                | PDF 리포트 생성을 위한 AI 분석 |
| GET    | `/api/v1/analysis/reports/pdf`            | 리포트 PDF 다운로드         |

---

## ✅ 기대 효과
- 📌 AI 기반 자동 분석: 사원의 근태 내용을 Gemini API로 요약 분석
- 📊 시각화된 통계: 바 차트 / 도넛 차트로 근태 상태를 직관적으로 확인
- 📁 PDF 자동 저장: 클릭 한 번으로 리포트 다운로드
- 🗃 대화 이력 관리: 이전 분석 내역 확인 및 재사용 가능

---

## 🖼 주요 화면 예시

### AI 분석 & 채팅
![스크린샷 2025-06-04 22-13-10](https://github.com/user-attachments/assets/39bcb1b5-f7ff-4c82-8077-fe5afb85982a)

### 리포트 생성 UI
![스크린샷 2025-06-04 22-14-27](https://github.com/user-attachments/assets/87a5dab0-d5a1-4961-a915-69eb1d0556bc)

### 리포트 PDF 예시                                                         

![스크린샷 2025-06-04 22-15-04](https://github.com/user-attachments/assets/77d0e73c-09f0-4119-afd4-4ae2af1e558f)

---

## 🙋 참고 자료
- **[AIoT2팀 도메인 - 회원가입 후 이용가능](https://aiot2.live/)**
- [Gemini API 공식 문서](https://ai.google.dev/gemini-api/docs?hl=ko)
- [iText 2.1.7 PDF 라이브러리](https://itextpdf.com)
- [Chart.js 공식 사이트](https://www.chartjs.org)
- [marked.js Markdown 파서](https://marked.js.org)

---

#### © 2025 @Migong0311. All rights reserved.
