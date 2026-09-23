# 화면설계서 매핑과 구현 범위

참조: `interior_screen_design_v1.pptx`, 40장, 화면설계 v1.0 (2026-09-23).
이 산출물은 **개발을 시작할 수 있는 MVC 초기 프로젝트**이며 전체 MVP 완성본이 아닙니다.
설계서의 문구는 요구사항 자료로만 사용했습니다. 외부 전송·운영 작업의 실행 지시로 해석하지 않았습니다.

## 실행 가능한 기반

| 화면 ID | 경로 | 컨트롤러 / 뷰 | 현재 범위 |
| --- | --- | --- | --- |
| AUTH-01 | /login | AuthController / auth/login | DB 계정 로그인, BCrypt, CSRF, 역할별 시작 화면, POST 로그아웃 |
| DASH-01 | /dashboard | DashboardController / dashboard/index | 고객·현장 수와 최근 현장. 일정·미수금은 미연결 표시 |
| CUS-01 | /customers | CustomerController / customer/list | 이름·연락처 검색, 20개 페이징, 최근 수정순, 연락처 마스킹 |
| CUS-02 | /customers/new, /customers/{id}/edit | CustomerController / customer/form | 등록·수정·필수값 검증·중복 연락처 안내·수정 충돌 방지 |
| CUS-03 | /customers/{id} | CustomerController / customer/detail | 고객 정보·현재 메모·연결 현장. 상담 이력·보관은 미구현 |
| PRJ-01 | /projects | ConstructionProjectController / project/list | 현장명·주소·상태 검색, 페이징. 고객명·기간 필터는 후속 |
| PRJ-02 | /projects/new, /projects/{id}/edit | ConstructionProjectController / project/form | 고객 연결, 기본정보 등록·수정, 희망 시공일·현장 조건 |
| PRJ-03 | /projects/{id} | ConstructionProjectController / project/detail | 기본정보와 업무 탭. 상태 전이·금액 요약·감사 이력은 후속 |

등록 폼의 요청 키로 같은 요청의 순차 재시도를 중복 저장하지 않습니다.
동시 등록의 고유키 충돌은 오류 안내 후 목록 확인으로 처리하며, 완전한 재시도 응답 재생은 후속 구현입니다.
수정에는 JPA `@Version`과 화면 버전을 검사합니다.
모든 실제 고객·현장 조회는 로그인한 직원의 업체 ID를 사용합니다.
작업자는 고객·현장 관리 및 금액 관련 URL에 접근할 수 없습니다.

## 준비된 후속 화면

아래는 **GET 경로와 Thymeleaf 준비 화면만 구현**했습니다. 버튼을 눌러 업무를 저장하는 기능은 아직 없습니다.
서로 다른 화면 파일과 도메인별 컨트롤러를 나눴으므로 각 화면부터 확장할 수 있습니다.

| 화면 ID | 화면 | 경로 | 컨트롤러 | View | 후속 구현 |
| --- | --- | --- | --- | --- | --- |
| SUR-01 | 공간별 실측 | `/projects/{projectId}/survey` | SurveyController | `survey/form.html` | 둘레·높이·개구부·천장 면적 입력 및 실측 버전 저장 |
| EST-01 | 견적 작성 | `/estimates` | EstimateController | `estimate/form.html` | 항목별 수량·단가 입력, 서버 금액 계산, 초안 저장 |
| EST-02 | 견적 미리보기 | `/estimates/preview` | EstimatePreviewController | `estimate/preview.html` | 업체·고객 정보와 견적 버전을 보존한 PDF 출력 |
| EST-03 | 견적 이력 | `/estimates/history` | EstimateHistoryController | `estimate/history.html` | 확정 견적 보존, 개정 사유와 버전별 조회 |
| CON-01 | 계약 기록 | `/contracts` | ContractController | `contract/form.html` | 확정 견적 연결, 동의 기록, 계약금·중도금·잔금 계획 |
| SCH-01 | 일정 캘린더 | `/schedules` | ScheduleController | `schedule/calendar.html` | 월·주·목록 조회와 일정 유형별 필터 |
| SCH-02 | 일정과 작업자 배정 | `/schedules/new` | ScheduleAssignmentController | `schedule/form.html` | 작업자별 시간 충돌 검증과 배정 알림 |
| WRK-01 | 작업 기록 | `/work-logs` | WorkLogController | `work/list.html` | 작업 일지·이슈 입력과 작성자별 수정 권한 |
| MAT-01 | 자재 기준정보 | `/materials` | MaterialController | `material/list.html` | 품명·품번·판매 단가·롤 기준 관리 |
| MAT-02 | 자재 발주 | `/purchase-orders` | PurchaseOrderController | `purchase/list.html` | 2차 기능: 발주·부분입고·반품 및 비용 연결 |
| FIN-01 | 비용과 예상 이익 | `/costs` | ProjectCostController | `cost/list.html` | 2차 기능: 비용 원장·증빙·정정 이력 |
| PAY-01 | 입금 계획과 수금 | `/payments` | PaymentController | `payment/list.html` | 분할 입금·환불·취소 원장과 미수금 집계 |
| QA-01 | 완료 점검 | `/completion` | CompletionController | `completion/form.html` | 필수 점검, 미해결 이슈 검사, 관리자 완료 승인 |
| AS-01 | A/S 접수 | `/after-service` | AfterServiceController | `afterservice/list.html` | 2차 기능: 완료 현장의 하자 접수와 처리 이력 |
| MOB-01 | 오늘 작업 | `/mobile/tasks` | MobileTaskController | `mobile/tasks.html` | 본인에게 배정된 일정만 조회 |
| MOB-02 | 작업 상세 | `/mobile/task-detail` | MobileTaskDetailController | `mobile/detail.html` | 배정 검증 후 작업 시작·완료 요청·일지 저장 |
| NTF-01 | 알림 | `/notifications` | NotificationController | `notification/list.html` | 수신자별 알림 조회·읽음 처리·중복 방지 |
| ADM-01 | 직원과 업체 설정 | `/settings` | SettingsController | `setting/index.html` | 직원 등록·비활성화, 마지막 관리자 보호, 업체 출력 정보 |

모바일 화면에는 현재 배정 데이터를 표시하지 않습니다. 배정 범위 검증과 응답 DTO를 구현하기 전에는 작업자의 데이터 API를 열지 마세요.
관리자 설정 화면도 준비 화면이므로 직원 생성·권한 변경·비활성화 UI는 후속입니다.
계정의 활성 상태와 역할 변경은 기존 세션의 다음 요청부터 재검사하도록 필터를 넣었습니다.

## 먼저 구현할 작업

1. 상담 이력 추가·수정 감사, 고객 보관, 고객·현장의 필터 확대.
2. 실측 저장 버전, 파일 첨부의 MIME·확장자·용량·소유 범위 검증.
3. 견적 항목/초안 저장, 확정 조건, 회사·항목 스냅샷, 복제 개정, PDF.
4. 계약·변경 계약과 결제 계획 합계 검증.
5. 작업자 배정 시 동시성 잠금과 충돌 확인, 본인 배정 조회 DTO.
6. 작업 시작·일지·완료 요청·관리자 승인, 필수 점검과 미해결 이슈 검사.
7. 수금 원장, 취소·환불·초과입금 정책, 재시도·알림 중복 방지.
8. 상태 전이·보류·취소와 감사 이력. 설계서 39쪽의 시나리오를 통합 테스트로 추가.
9. 2차 기능: 발주·부분입고·실제 원가·A/S·사진 분류.

실측 면적, 롤 수량, 견적 계산, 일정의 시간 겹침은 독립 서비스와 테스트가 준비되어 있습니다.
저장·상태 변경 서비스와 연결하는 것은 후속 작업입니다.
