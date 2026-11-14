# 운영 DB 분리 및 마이그레이션 기록

실행일: 2026-10-16 (Asia/Seoul)

## 환경

| 용도 | Supabase 프로젝트 | 프로젝트 ID |
| --- | --- | --- |
| 개발 / 원본 | roofandwall | nswozvligsydqweriova |
| 운영 | interior-cms-prod | mldcttpgrfswburnazrz |

운영 프로젝트는 roofandwall 조직의 Free 플랜, 서울 리전(ap-northeast-2)에 생성했다.

## 적용 범위

- `interior_cms` 스키마의 테이블 28개와 IDENTITY 시퀀스 26개.
- 기본키, 고유키, CHECK 및 외래키 제약 조건. 외래키 37개.
- 제약 조건에 포함되지 않는 추가 인덱스 12개.
- 기존 직원 계정 1건. ID, company_id, BCrypt 비밀번호 해시와 나머지 계정 필드를 그대로 복사했다.
- 직원 계정 시퀀스는 복사한 최대 ID 이후 번호를 발급하도록 보정했다.
- 고객, 현장 및 나머지 테이블 데이터는 복사하지 않았다.
- 원본 DB에는 읽기 전용 SELECT만 실행했다. 로컬 `.env`와 앱 실행 설정은 변경하지 않았다.

구조 생성 SQL: [2026-10-16-production-schema.sql](sql/2026-10-16-production-schema.sql).
이 SQL은 비어 있는 DB에 한 번 적용하는 용도다. 이미 적용한 운영 DB에 다시 실행하지 않는다.
직원 계정의 비밀번호 해시와 개인 정보가 포함된 이전 SQL은 저장소에 기록하지 않았다.

## 검증 결과

| 항목 | 결과 |
| --- | --- |
| 원본/운영 구조 생성 SQL 지문 비교 | 일치 (`a10768a09712f6cd78ba8595f0fd2297`) |
| 원본/운영 직원 전체 행 지문 비교 | 일치 |
| 운영 테이블 / 시퀀스 / 외래키 | 28 / 26 / 37 |
| 운영 직원 계정 | 1건 |
| 직원 계정을 제외한 모든 테이블 데이터 합계 | 0건 |
| 직원 시퀀스 상태 | 다음 발급 ID가 기존 최대 ID보다 큼 |
| anon / authenticated의 스키마 USAGE | 모두 false |
| anon / authenticated의 직원 테이블 SELECT | 모두 false |

테이블 RLS 상태는 원본과 동일하다. 앱 스키마에 대한 anon/authenticated 접근 권한을 추가하지 않았다.
Supabase Auth 사용자 복사가 아니라 앱의 `interior_cms.staff_accounts` 계정을 이전한 것이다.
원본 스키마에는 별도 업체 마스터 테이블이나 직원 계정에서 다른 업체 테이블로 향하는 외래키가 없으므로, 직원의 기존 `company_id` 값을 유지했다.

## 현재 작업의 한계와 후속 배포

- DB 분리와 마이그레이션까지 완료했으며, 서버 배포 및 앱의 운영 DB 연결 전환은 하지 않았다.
- 실제 운영 서버에는 이 운영 프로젝트의 DB 연결 값을 환경 변수로 등록해야 한다. 개발 PC의 `.env`는 개발 프로젝트 연결을 유지한다.
- 운영 DB를 연결한 앱에서의 실제 로그인 테스트는 서버 연결 설정 후 별도로 수행해야 한다. 이번에는 계정 행과 비밀번호 해시의 동일성까지 검증했다.
- `flyway_schema_history` 테이블 구조는 복사했지만 과거 실행 이력은 복사하지 않았다. 현재 앱은 자동 DDL 및 Flyway를 사용하지 않으므로, 이후 Flyway를 도입한다면 현재 구조에 맞춰 baseline을 설정해야 한다.
- 이 작업은 Supabase 플랫폼 전체, Storage 파일 또는 별도 외부 서비스의 이전을 포함하지 않는다.
