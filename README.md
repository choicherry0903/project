# 지붕과벽 CMS

고객·현장 목록, 현장 상세, AI 상담 정리의 **화면 골격만** 준비한 프로젝트입니다.
Java 25 / Spring Boot 4.0.8 / Maven Wrapper / Thymeleaf를 사용합니다.

## 실행

프로젝트 루트에서:

```powershell
.\scripts\run-local.ps1
```

[고객·현장 목록](http://localhost:8080/projects)에서 시작합니다.
기본값은 `dev` 프로필입니다. DB, 로그인, API 키, `.env` 설정이 필요 없습니다.
서버는 로컬 확인용으로 `127.0.0.1:8080`에 열립니다. 종료는 실행 터미널에서 Ctrl+C입니다.

IntelliJ에서는 Maven 프로젝트를 다시 로드하고 JDK 25를 지정한 뒤
공유 실행 구성 **Interior CMS Local**을 사용하세요.
이전 Demo / Supabase 실행 구성은 삭제했습니다.

스크립트 없이 실행하려면 JDK 25의 `JAVA_HOME`을 지정한 뒤:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
```

## 화면과 파일

| 화면 | 경로 | Controller | HTML |
| --- | --- | --- | --- |
| 고객·현장 목록 | `/projects` | `project/controller/ProjectListController.java` | `templates/project/list.html` |
| 현장 상세 | `/projects/detail` | `project/controller/ProjectDetailController.java` | `templates/project/detail.html` |
| AI 상담 정리 | `/consultations/ai` | `consultation/controller/AiConsultationController.java` | `templates/consultation/ai.html` |

Java 파일의 기준 경로는 `src/main/java/com/roofandwall/interior/`,
HTML 파일의 기준 경로는 `src/main/resources/`입니다.
공통 메뉴는 `templates/layout/base.html`, 스타일은 `static/css/app.css`입니다.
`/`는 목록으로 이동합니다. 상세 경로는 데이터 연결 전 화면 확인용 고정 경로입니다.

검색·등록·저장·파일 업로드·AI 실행은 구현하지 않았으며 입력과 버튼은 비활성 상태입니다.
예시 고객 데이터나 가짜 AI 결과도 표시하지 않습니다.
Service, Repository, Entity, DTO는 실제 데이터와 기능 요구사항이 정해진 뒤 추가합니다.

## 정리 범위

기존 인증, 고객/현장 CRUD, 자재, 실측, 견적, 계약, 일정, 수금, AS 등의 구현과
관련 테스트·템플릿·JS·DB 마이그레이션·연동 설정·문서를 제거했습니다.
관련 Security, JPA, Flyway, DB 드라이버, SMTP, S3 및 이미지 처리 의존성도 제거했습니다.

이전 코드는 Git 커밋 `ae4f547e310c222bbf3a46e6f0b455ed08edb4a7`에서 확인할 수 있습니다.
실제 DB나 외부 저장소의 데이터를 삭제하는 작업은 수행하지 않았습니다.
로컬 `.env`와 IDE 설정, 개발 도구 및 Maven 캐시는 보존하되 앱에서 `.env`를 읽지 않습니다.
기존 마이그레이션을 그대로 운영 DB에 다시 적용하지 마세요. DB 연결은 이후 별도 설계가 필요합니다.

## 검증

```powershell
.\scripts\run-local.ps1 -Task test
.\scripts\run-local.ps1 -Task package
```

테스트/패키징은 `clean`부터 실행하여 이전 클래스와 리소스가 산출물에 남지 않도록 합니다.
테스트는 세 화면의 실제 Thymeleaf 렌더링, 세 메뉴와 활성 표시, CSS 제공,
이전 화면 제거 및 저장 경로 미구현 상태를 검증합니다.
산출물: `target/interior-cms-0.0.1-SNAPSHOT.jar`.

`dev`에서는 HTML/CSS 저장 후 새로고침으로 반영됩니다.
Java 변경은 IDE 빌드 이후 DevTools가 재시작합니다.

다음 개발 범위는 [화면 구성](docs/screen-map.md)과 [구조](docs/architecture.md)를 참고하세요.
