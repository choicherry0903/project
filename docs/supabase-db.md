# Supabase PostgreSQL

Spring Boot가 PostgreSQL JDBC로 접속합니다. 기존 MVC·Spring Security 로그인과 업체별 접근 검사를 사용하며 Supabase Auth나 브라우저 Data API를 사용하지 않습니다.

## 연결 정보

| 항목 | 값 |
| --- | --- |
| 방식 | Session pooler |
| 호스트 | aws-0-ap-northeast-2.pooler.supabase.com |
| 포트 | 5432 |
| 데이터베이스 | postgres |
| 사용자 | .env의 DB_USERNAME (postgres.프로젝트참조 형식) |
| 비밀번호 | .env의 DB_PASSWORD |
| 업무 스키마 | interior_cms |
| SSL | require |

SSL require는 암호화를 강제합니다. 서버 인증서까지 검증하려면 Supabase 인증서를 준비하고 JDBC `verify-full`과 신뢰 CA를 설정해야 합니다.
Session pooler는 IPv4에서 접근 가능하며 준비된 쿼리와 세션 기능을 지원합니다. 이 프로젝트에서는 transaction pooler(6543)를 사용하지 않습니다.

## 실행

```powershell
.\scripts\run-local.ps1 -Profile supabase
```

IntelliJ에서는 `Interior CMS Supabase`를 실행합니다. 작업 디렉터리는 프로젝트 루트입니다.
첫 실행은 Flyway V1로 업무 테이블 23개를 만들고 V2로 API 역할의 스키마 접근을 차단합니다.
`interior_cms`를 Supabase Data API의 Exposed schemas에 추가하지 마세요.
시간은 PostgreSQL `timestamp with time zone`과 Java Instant로 저장하고 ID는 identity를 사용합니다.

## DataGrip

PostgreSQL 데이터 소스를 추가하고 위 연결 정보를 입력합니다. SSH는 사용하지 않습니다.
SSL을 require로 설정하고 Schemas에서 `interior_cms`를 선택합니다.
웹 로그인은 `.env`의 ADMIN_EMAIL/ADMIN_PASSWORD로 초기 생성되며, DB 비밀번호와는 다릅니다.

## 공동 개발과 배포

담당자가 `supabase` 프로필로 마이그레이션을 먼저 적용합니다.
다른 개발자는 `supabase,shared`로 실행하면 스키마 변경 없이 검증만 수행합니다.
현재 제공된 postgres 계정은 관리자 계정이므로 개발자에게 배포할 때에는 별도 제한 계정을 발급하는 것이 좋습니다.
앱 연결 풀은 프로세스당 최대 3개입니다. 여러 개발자와 앱 인스턴스의 연결 수를 합산해서 관리합니다.
실제 서비스에서는 HTTPS 뒤에서 `supabase,prod` 프로필을 사용하고 환경변수로 비밀번호를 주입합니다.

## 변경 범위

기존 DB의 업무 데이터는 이관하지 않고 Supabase에 새 스키마와 최초 관리자만 준비합니다.
Supabase의 `auth`, `storage`, `public` 등 기존 스키마는 수정하지 않습니다.
로컬 DB 서버와 VPN 프로그램 제거는 프로젝트 설정 정리와 별개입니다.

공식 자료: https://supabase.com/docs/guides/database/connecting-to-postgres
