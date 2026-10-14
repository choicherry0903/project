# 초기 화면 구조

## 기술 기반

Java 25, Spring Boot 4.0.8, Spring MVC, Thymeleaf, Maven Wrapper.
Java 패키지는 main의 업체명 변경을 반영한 `com.roofandwall.interior`를 사용합니다.
화면의 업체명은 지붕과벽입니다.

```text
src/main/java/com/roofandwall/interior/
  InteriorCmsApplication.java
  project/controller/
    ProjectListController.java
    ProjectDetailController.java
  consultation/controller/
    AiConsultationController.java
src/main/resources/
  application.yml
  application-dev.yml
  templates/layout/base.html
  templates/project/list.html
  templates/project/detail.html
  templates/consultation/ai.html
  static/css/app.css
src/test/java/com/roofandwall/interior/
  ScreenScaffoldIntegrationTest.java
scripts/run-local.ps1
.run/Interior CMS Local.run.xml
```

Controller는 View 이름만 반환합니다. 루트의 목록 이동 외에는 처리 로직이 없습니다.
HTML은 Thymeleaf fragment로 공통 head와 세 메뉴, 준비 중 안내를 공유합니다.
현재 데이터를 읽는 Service/Repository나 저장용 Entity/DTO는 필요하지 않으므로 생성하지 않습니다.
업무 기능 확정 후 해당 계층을 추가합니다.

## 데이터 및 외부 연동

DB, Flyway, 인증, SMTP, S3, AI SDK/API 연결이 없습니다.
화면 확인을 위해 로그인이나 DB 연결 정보가 필요하지 않습니다.
운영 배포 전에는 인증·권한과 데이터 보호를 실제 요구사항에 맞춰 추가해야 합니다.

기존 마이그레이션은 소스에서 제거했지만 실제 DB는 변경하지 않았습니다.
향후 새 모델의 DB 연결은 기존 스키마와의 관계를 검토한 뒤 별도로 진행합니다.
로컬 `.env`는 실행 스크립트와 앱이 읽지 않습니다.

## 검증

전체 Spring 컨텍스트와 MockMvc로 실제 View 렌더링을 확인합니다.
과거 URL이 사라졌는지와 세 화면에 POST 처리 경로가 없는지도 확인합니다.
`clean` 빌드로 과거 컴파일 파일과 SQL/템플릿이 패키지에 포함되지 않게 합니다.
