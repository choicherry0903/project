# 목록 조회와 등록 화면 개발 안내

## 완료된 경로

| 요청 | 동작 |
| --- | --- |
| GET / | /projects로 이동 |
| GET /projects | 기존 DB 목록 조회 후 project/list 렌더링 |
| GET /projects/new | 입력용 project/form 렌더링 |
| GET /projects/detail | 기존 상세 화면 골격 유지 |

목록은 전체 조회이며 현장 ID 내림차순입니다.
검색·페이지 분할·개별 상세 조회는 이번 범위에 없습니다.

## 기존 DB 조회

`interior_cms.construction_projects p`와 `interior_cms.customers c`를
`p.customer_id = c.id AND p.company_id = c.company_id`로 조인합니다.
`p.company_id = 로그인 직원의 company_id`이며 보관되지 않은 고객만 표시합니다.
주소는 address와 detail_address를 합칩니다.

| 표시 항목 | 실제 컬럼 |
| --- | --- |
| 현장 ID | p.id |
| 현장명 | p.name, 최대 100자 |
| 고객명 | c.name, 최대 50자 |
| 연락처 | c.phone, 최대 15자 |
| 현장 주소 | p.address + p.detail_address, 각 최대 200자 |
| 상태 | p.status |

기존 상태를 그대로 해석합니다: CONSULTATION=상담,
SURVEY_SCHEDULED=현장 확인 예정, ESTIMATING=견적, CONTRACTED=계약,
IN_PROGRESS=시공, COMPLETED=완료·AS, CANCELLED=취소.

앱은 SELECT만 수행하며 자동 테이블 생성/마이그레이션은 비활성입니다.
테스트용 최소 스키마는 src/test/resources/test-schema.sql에만 있습니다.
이는 운영 테이블 생성용 DDL이 아닙니다.

## 사용자가 이어서 구현할 부분

1. 등록 입력 DTO와 서버 검증: customerName, customerPhone, projectName, siteAddress
2. POST /projects Controller 및 등록용 트랜잭션 Service
3. 기존 customers와 construction_projects에 맞는 INSERT 및 고객-현장 연결
4. 성공 시 redirect:/projects, 실패 시 입력값과 오류 표시
5. 등록 버튼 활성화, 수정·삭제 처리 및 UI

form.html의 필드 name은 위 DTO 후보와 동일합니다.
현재 form action은 /projects, method는 post지만 저장 버튼은 disabled이며 처리 경로가 없습니다.
입력 내용은 저장되지 않습니다.

기존 DB는 화면의 네 필드 외에도 company_id, created_at, updated_at,
request_key, archived(고객), construction_type, occupied, on_hold(현장) 등
필수 컬럼이 있으므로 등록 Service에서 적절한 값을 제공해야 합니다.
스키마를 간소화하거나 필수 제약을 삭제하지 않았습니다.
상태는 서버에서 CONSULTATION으로 지정하고,
고객 생성/재사용 정책과 업체 범위도 등록 구현에서 확정하세요.

ProjectRepository는 SELECT만 제공하며 ProjectQueryService는 readOnly 트랜잭션입니다.
등록용 Service와 SQL을 별도로 추가하면 됩니다.
required/maxlength만으로 서버 검증을 대신할 수 없습니다.
