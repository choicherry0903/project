# 현재 구조

Java 25, Spring Boot 4.0.8, Spring MVC, Thymeleaf, Spring JDBC, Supabase PostgreSQL.
Java 패키지는 `com.roofandwall.interior`입니다.

메뉴별 패키지는 `customer`(고객·현장 목록 및 등록), `scene`(현장),
`aicounsult`(AI 상담 정리)로 나눕니다.
컨트롤러는 메뉴별로 `CustomerController`, `SceneController`, `AICounsultController` 하나씩 둡니다.
각 메뉴의 목록·등록·수정·삭제는 기능별 컨트롤러를 나누지 않고 해당 컨트롤러의 메서드로 관리합니다.

## 목록 조회

`CustomerController → CustomerQueryService → CustomerRepository → Supabase`

기존 interior_cms 스키마의 construction_projects와 customers를 조인합니다.
Repository는 목록과 상담 항목을 조회하며 DTO CustomerItem으로 반환합니다.
조회 Service는 readOnly 트랜잭션으로 동작합니다.
로그인 직원의 업체 ID로 제한하고 보관 고객은 제외합니다.
템플릿은 th:text로 고객 입력을 이스케이프하고 빈 목록을 별도로 표시합니다.

## 화면

- customer/customer-list.html: 실제 DB 목록과 등록 화면 진입
- customer/customer-registration.html: 고객·현장·상담 등록 및 오류 시 입력 복원
- customer/customer-detail.html: 등록과 같은 고객·현장·상담 필드로 구성한 읽기 전용 상세 조회
- ai-counsult/ai-counsult.html: 기존 AI 상담 골격
- layout/base.html: 공통 head, 세 메뉴, 골격 화면용 안내
- static/css/app.css: 공통 스타일과 반응형 목록/폼

CustomerController에서 목록(GET /customers)과 등록 화면(GET /customers/regform)을 함께 제공합니다.
등록·수정은 쓰기 트랜잭션으로 고객·현장·상담 항목을 함께 저장합니다. SceneController가 projectId로 업체 범위 안의 현장과 고객·상담을 조회합니다. CustomerController가 수정·삭제를 처리하며 삭제는 고객의 archived 컬럼을 사용해 기록을 보관합니다. 기존 version 컬럼으로 변경 충돌을 감지합니다. 검색, AI 실행은 아직 구현하지 않았습니다.

## 설정과 테스트

application.yml이 .env 또는 환경 변수에서 Supabase 연결 정보를 읽습니다.
자동 DDL/마이그레이션은 비활성입니다. 추가 상담 테이블 DDL은 docs/sql에 있습니다.
업체 범위는 로그인 직원의 company_id로 결정합니다.
서버는 로컬 검토용으로 127.0.0.1에 바인딩합니다.

테스트는 test 프로필의 메모리 H2로 실제 조회·출력·정렬·업체 범위를 검증합니다.
테스트 전용 스키마로 조회·등록·중복 방지·검증·롤백을 확인하며 실제 DB 데이터는 변경하지 않습니다.
등록 개발 연결 위치는 [목록 개발 안내](customer-list.md)에 정리했습니다.
