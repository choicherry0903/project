# 초기 프로젝트 검증 기록

검증일: 2026-09-23, Windows, Oracle JDK 25.0.4.1, Spring Boot 4.0.8.

- Maven `package`: 성공, 실행 가능한 Spring Boot JAR 생성.
- 테스트: 총 14개, 실패 0, 오류 0.
- 업무 계산 4개: 설계서의 2,178,000원 견적, 원 단위 반올림·할인·상한, 33.40㎡ 실측·5롤 산출, 일정 겹침과 경계 시간.
- 통합 테스트 10개: 인증·CSRF, DB 비밀번호 로그인, 고객→현장 등록 및 화면 렌더링, 입력 검증, 동일 요청 재시도, 수정 충돌, 업체 격리, 작업자 권한, 사무 담당 권한, 준비 화면 경로.
- 테스트 프로필에서 동일 Flyway V1 SQL을 H2 MySQL 호환 모드에 적용하고 JPA `validate` 통과.
- 패키징한 JAR를 `demo` 프로필로 구동하여 실제 브라우저 로그인과 대시보드 렌더링 확인.
- Computer Use로 IntelliJ의 `pom.xml`을 Maven 프로젝트로 추가한 상태 확인.
- IntelliJ의 `Interior CMS Demo` 구성에서 모듈 SDK Java 25, 메인 클래스, `--spring.profiles.active=demo`를 확인하고 IDE 실행 버튼으로 구동. IntelliJ Java agent가 붙은 프로세스와 `/login` HTTP 200 응답 확인 후 IDE 중지 버튼으로 종료, 8080 포트 해제 확인.

실제 MySQL 서버는 생성하거나 연결하지 않았습니다. 따라서 MySQL 서버에서의 마이그레이션·드라이버 통합 검증은 아직 하지 않았습니다.
아직 개발하지 않은 업무 기능은 테스트 완료 범위에 포함하지 않습니다. 상세 범위는 `screen-map.md`에 명시했습니다.
