# Supabase 전환 검증 기록

검증일: 2026-09-29, Windows, JDK 25.0.4.1, Spring Boot 4.0.8.

- PostgreSQL JDBC 42.7.13 및 Flyway PostgreSQL 모듈 적용.
- Maven package 성공, 테스트 14개(업무 정책 4개, MVC/DB 통합 10개) 모두 통과.
- H2 PostgreSQL 호환 모드에서 공통 Flyway V1 적용 및 Hibernate 스키마 검증 통과.
- 실제 Supabase PostgreSQL 17.6의 Session pooler(5432)에 JDBC sslmode=require로 접속 성공.
- interior_cms 스키마에 업무 테이블 23개와 flyway_schema_history 생성, V1/V2 적용 성공.
- 실제 Supabase 연결로 Hibernate 스키마 검증 및 Spring Boot 시작 성공.
- 최초 웹 관리자 생성, CSRF를 포함한 실제 비밀번호 로그인과 인증 후 대시보드 HTTP 200 확인.
- PostgreSQL identity ID와 시간대 포함 timestamp, INSERT/UPDATE/DELETE 트랜잭션 검증 통과. 검증 트랜잭션은 롤백.
- anon/authenticated 역할의 interior_cms 스키마 USAGE 권한이 false임을 확인.
- pg_stat_ssl은 풀러→DB 내부 연결을 보여주며 해당 연결은 false로 보고됨. 클라이언트→풀러 구간은 JDBC sslmode=require로 암호화를 강제함. 서버 인증서 신원 검증은 별도 설정 필요.

기존 업무 데이터는 이관하지 않았습니다. 기존 DB에서 사용하던 V1 이력을 재사용하지 않고 신규 PostgreSQL 스키마에 적용했습니다.
아직 개발하지 않은 업무 기능은 테스트 완료 범위에 포함하지 않습니다. 상세 범위는 screen-map.md를 확인하세요.
