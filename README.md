# 지붕과벽 CMS

Java 25 / Spring Boot 4.0.8 / Spring MVC / Thymeleaf / Spring JDBC / Supabase PostgreSQL.

## 현재 구현 범위

- **목록 조회**: 기존 Supabase 고객·현장 테이블을 조인해 현장 ID 내림차순으로 표시
- **목록 화면**: 현장명, 고객명, 연락처, 현장 주소, 진행 상태와 전체 건수
- **등록 화면**: 고객명, 연락처, 현장명, 현장 주소 입력 및 목록/취소 이동
- 고객 상세: 상담·견적·계약·자재·시공·검수와 수금 현황의 HTML 미리보기
- AI 상담 정리: 기존 화면 골격 유지

등록·수정·삭제 처리와 검색·파일 업로드·AI 호출은 구현하지 않았습니다.
등록 버튼은 화면으로 이동하며, 등록 화면의 저장 버튼은 비활성입니다.
상세 조회는 이번 범위가 아니므로 목록의 개별 행에 상세 링크를 연결하지 않았습니다.
메뉴는 기존 세 개를 유지하고 등록 화면은 목록에서 진입합니다.

## 실행

프로젝트 루트에서:

```powershell
.\scripts\run-local.ps1
```

- [고객](http://localhost:8080/customers)
- [고객 등록](http://localhost:8080/customers/regform)
- [고객 상세 미리보기](http://localhost:8080/customers/detail)
- [AI 상담 정리 골격](http://localhost:8080/aicounsult)

기본값은 dev 프로필이며 서버는 `127.0.0.1:8080`에 열립니다.
JDK 25가 필요합니다. IntelliJ는 Maven 재로드 후 **Interior CMS Local**을 실행합니다.

## Supabase 연결

프로젝트 루트의 기존 `.env`에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 읽습니다.
DB_URL은 `jdbc:postgresql://...` 형식이며 비밀번호는 별도 키에 둡니다.
환경 변수로도 설정할 수 있으며 실제 비밀값은 Git에 저장하지 않습니다.

목록은 `interior_cms.construction_projects`와 `interior_cms.customers`를 읽습니다.
자동 DDL, Flyway, 샘플 데이터 추가는 없으며 기존 테이블과 데이터를 변경하지 않습니다.
보관 고객(`archived=true`)은 제외합니다.
`로그인 직원의 company_id`에 해당하는 현장만 읽으며 세션의 직원 계정에서 업체 ID를 가져옵니다.
로그인하지 않은 요청은 /login으로 이동합니다.
업체 ID는 요청 파라미터나 환경 변수로 바꿀 수 없습니다.

## 구조

```text
src/main/java/com/roofandwall/interior/customer/
  controller/CustomerController.java
  service/CustomerQueryService.java
  repository/CustomerRepository.java
  dto/CustomerListItem.java
src/main/java/com/roofandwall/interior/scene/
  controller/SceneController.java
src/main/java/com/roofandwall/interior/aicounsult/
  controller/AICounsultController.java
src/main/resources/templates/customer/
  customer-list.html
  customer-registration.html
  customer-detail.html
src/main/resources/templates/ai-counsult/
  ai-counsult.html
```

메뉴별 Java 파일 접두어는 `Customer`, `Scene`, `AICounsult`로 구분합니다.
고객 HTML 파일은 `customer` 폴더에서 목록·등록·상세로 구분합니다.
컨트롤러는 메뉴마다 하나씩 두고, 목록·등록·수정·삭제 메서드를 같은 컨트롤러에서 관리합니다.

목록은 Controller → 조회 Service → JDBC Repository → 기존 Supabase 테이블 순서로 읽습니다.
등록 화면은 GET /customers/regform만 있으며 POST 처리나 저장 쿼리는 없습니다.
등록 기능을 이어서 만들 위치와 기존 DB 필수 컬럼은 [목록 개발 안내](docs/customer-list.md)를 참고하세요.

## 검증

```powershell
.\scripts\run-local.ps1 -Task test
.\scripts\run-local.ps1 -Task package
```

테스트는 `test` 프로필과 별도 메모리 H2를 사용하므로 Supabase 연결 설정이 없어도 실행됩니다.
H2 드라이버와 테스트 스키마는 테스트 전용이며 실행 JAR에 포함하지 않습니다.
조회·정렬·빈 목록·업체 범위·보관 고객 제외·HTML 이스케이프,
등록 화면과 저장 미구현 상태, 기존 세 메뉴 및 화면 렌더링을 검증합니다.
테스트 데이터는 테스트 트랜잭션으로만 삽입 후 롤백합니다.
산출물은 `target/interior-cms-0.0.1-SNAPSHOT.jar`입니다.

HTML/CSS는 dev 프로필에서 저장 후 새로고침, Java는 IDE 빌드 이후 DevTools 재시작으로 반영됩니다.

## 로그인

/login에서 기존 Supabase interior_cms.staff_accounts의 아이디와 비밀번호를 사용합니다.
아이디는 앞뒤 공백과 대소문자를 구분하지 않으며 기존 BCrypt 해시를 검증합니다.
비활성 계정 또는 이메일이 있으나 확인되지 않은 계정은 로그인할 수 없습니다.
계정 생성·비밀번호 초기화·이메일 발송·회원가입은 추가하지 않았습니다.
로그인 성공 후 /customers로 이동하며, 메뉴의 로그아웃 버튼으로 세션을 종료합니다.
업무 화면은 로그인이 필요하고 조회 업체는 로그인 직원 계정의 company_id입니다.
POST 요청은 CSRF 토큰이 필요합니다. 사용자명 표시와 로그아웃은 공통 메뉴에 포함됩니다.
