# 바른시공 · Interior CMS

도배·인테리어 화면설계서 v1.0을 바탕으로 만든 **Java 25 / Spring Boot MVC 초기 프로젝트**입니다.
고객·현장 기본 등록과 조회를 실행할 수 있으며, 나머지 업무는 화면·모델 구조와 개발 항목을 준비했습니다.
전체 MVP가 완성된 상태는 아닙니다. [화면별 구현 범위](docs/screen-map.md)를 확인하세요.

## 기술 구성

| 항목 | 설정 |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.0.8 |
| 빌드 | Maven Wrapper, 별도 Maven 설치 불필요 |
| MVC View | Thymeleaf, HTML/CSS/JavaScript |
| Model | Spring Data JPA, Hibernate, Jakarta Validation |
| 인증 | Spring Security, 세션, BCrypt, CSRF |
| 운영 DB | Supabase PostgreSQL, Session pooler |
| DB 변경 관리 | Flyway, `ddl-auto: validate` |
| 로컬 화면 확인 | 명시적인 `demo` 프로필, 메모리 H2 |

Spring Boot 4 계열의 Java 25 지원은 [공식 발표](https://spring.io/blog/2025/11/20/spring-boot-4-0-0-available-now/)를 확인했습니다.
실제 의존성 4.0.8을 받아 설치된 JDK 25에서 검증했습니다.

## DB 없이 바로 실행

프로젝트 루트의 PowerShell에서 실행합니다. 스크립트가 `JAVA_HOME` 또는 일반 JDK 설치 경로에서 JDK 25를 찾습니다.

```powershell
.\scripts\run-local.ps1 -Profile demo
```

- 접속: <http://localhost:8080>
- 데모 이메일: `admin`
- 데모 비밀번호: `DemoAdmin!2026`
- 필요하면 `DEMO_PASSWORD` 환경 변수로 비밀번호를 바꿀 수 있습니다.
- 데모는 `127.0.0.1`에만 열리고 **프로세스 종료 시 데이터가 사라집니다**.
- 예시 고객·계약·입금 데이터는 자동 생성하지 않습니다.
- 중지: 실행 터미널에서 `Ctrl+C`.

PowerShell 정책 때문에 `.ps1`을 실행할 수 없으면, 정책을 바꾸지 않고 다음 명령을 사용하세요.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.4.1' # 본인의 JDK 경로
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=demo'
```

## Supabase 연결

Supabase 프로젝트의 **Connect → Session pooler**에 표시되는 주소를 사용합니다.
현재 구성은 서울 리전의 세션 풀러(5432)이며 VPN이나 SSH 터널이 필요하지 않습니다.

1. 처음 설정하는 개발자는 `.env.example`을 `.env`로 복사합니다. 기존 `.env`는 덮어쓰지 않습니다.
2. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 자신의 Supabase 연결 정보로 채웁니다.
3. 최초 웹 관리자용 `ADMIN_USERNAME`, `ADMIN_PASSWORD`(12자 이상)를 설정합니다.
4. 다음 명령으로 실행합니다.

```powershell
.\scripts\run-local.ps1 -Profile supabase
```

`supabase`가 기본 프로필입니다. Flyway가 `interior_cms` 스키마와 테이블을 만들고 JPA가 매핑을 검증합니다.
비밀번호는 DB 연결용 비밀번호이며 Supabase API 키가 아닙니다. 비밀번호를 URL에 넣지 않습니다.
`.env`는 Git에서 제외되며 실행 스크립트와 Spring Boot가 프로젝트 루트에서 읽습니다.
최초 웹 관리자만 자동 생성하고, 기존 계정 비밀번호는 덮어쓰지 않습니다.

## 회원가입 이메일 확인

새 직원은 이름·전화번호·이메일·아이디·비밀번호를 입력합니다. 가입 직후에는 로그인할 수 없고,
받은 메일의 링크에서 확인 버튼을 누르면 계정이 활성화됩니다. 인증 링크는 24시간 동안 유효하며 한 번만 사용할 수 있습니다.
메일을 받지 못했을 때는 로그인 화면의 **인증 메일 재발송**을 이용할 수 있습니다.

메일 발송에는 Resend SMTP를 사용합니다. Resend에서 발신 도메인을 확인하고 API 키를 만든 뒤
`.env` 또는 서버 환경 변수에 `RESEND_API_KEY`, `MAIL_FROM`, `APP_PUBLIC_BASE_URL`을 설정하세요.
`MAIL_FROM`은 확인된 도메인의 주소, `APP_PUBLIC_BASE_URL`은 사용자가 열 수 있는 HTTPS 주소여야 합니다.
설정이 없거나 발송에 실패하면 회원가입을 완료하지 않습니다. 기존 직원·관리자 계정은 이메일이 없을 수 있으며,
이메일 확인 여부가 기존 로그인에 영향을 주지 않습니다. 이메일 확인은 메일함 사용 권한만 증명하며 직원 신원 확인이나 가입 승인을 대신하지 않습니다.

자세한 DataGrip·공동 개발 설정은 [Supabase DB 안내](docs/supabase-db.md)를 확인하세요.

## IntelliJ IDEA

1. 루트 `pom.xml`을 Maven 프로젝트로 가져옵니다. `Add as Maven Project` 또는 `Load Maven Project`를 사용합니다.
2. Project SDK와 Maven Runner JRE를 **JDK 25**로 지정합니다.
3. Maven 재동기화 후 공유 실행 구성 `Interior CMS Demo`를 실행합니다.
4. 실행 구성이 모듈을 찾지 못하면 `Use classpath of module`에서 Maven 모듈 `interior-cms`를 선택합니다.
5. 실제 DB 실행은 `Interior CMS Supabase` 공유 실행 구성을 선택합니다.

현재 IntelliJ 상단 실행 구성은 `Interior CMS Demo`로 선택해 두었습니다.
오른쪽 초록색 ▶ 버튼(기본 단축키 `Shift+F10`)으로 실행하고, 실행 중 표시되는 빨간색 ■ 버튼으로 중지합니다.
시작 로그가 표시되면 <http://localhost:8080>에 접속하세요. 로그는 하단 `실행` 창에서 확인합니다.
IntelliJ에서 JDK 25와 `demo` 프로필로 직접 실행하여 로그인 페이지 HTTP 200 응답을 확인한 뒤 중지했습니다.
별도 외장 Tomcat 설정은 필요하지 않습니다. Supabase 연결 없이 화면만 확인하려면 `Interior CMS Demo`를 사용하세요.

기존 IDE 설정과 Git 변경 사항은 보존했습니다. `.idea`, 로컬 비밀번호, Maven 캐시, 빌드 산출물은 Git 제외 대상입니다.

## 개발 중 변경 자동 반영

IntelliJ 공유 실행 구성은 `demo,dev` 또는 `supabase,dev`로 실행합니다. DevTools 의존성이 추가되었으므로 처음 한 번은 Maven 재동기화 후 실행 중인 앱을 다시 실행하세요.

- HTML/CSS/JS: 원본 파일을 직접 읽고 캐시를 사용하지 않습니다. 저장 후 브라우저를 새로고침하면 반영됩니다.
- Java: IntelliJ 자동 빌드로 클래스가 변경되면 DevTools가 애플리케이션을 자동 재시작합니다. 컴파일 오류가 없어야 반영됩니다.
- YAML: 활성 프로필의 설정 파일이 빌드 출력에 복사되면 자동 재시작하여 다시 읽습니다. 환경 변수나 실행 인자 변경은 실행 구성을 다시 시작해야 합니다.

IntelliJ에서 **설정 → 빌드, 실행, 배포 → 컴파일러 → 프로젝트 자동 빌드**, **설정 → 고급 설정 → 컴파일러 → 애플리케이션 실행 중에도 자동 빌드 허용**을 켭니다. 다른 개발자의 IDE에서도 각각 설정해야 합니다. 저장 후 잠시 기다리거나 `Ctrl+F9`로 빌드할 수 있습니다.

Java/YAML은 무중단 변경이 아니라 수동 중지·실행을 대신하는 자동 재시작입니다. 재시작 중에는 잠시 요청이 중단될 수 있습니다. `demo,dev`는 자동 재시작 시 H2 데이터를 유지하지만 JVM을 종료하면 데이터가 사라집니다.

터미널에서는 `.\scripts\run-local.ps1 -Profile 'supabase,dev'` 또는 `-Profile 'demo,dev'`로 실행하세요. 스크립트 기본값은 `supabase,dev`입니다. 터미널 실행은 소스를 자동 컴파일하지 않으므로 Java/YAML 변경 후 별도 터미널에서 `.\mvnw.cmd compile`을 실행해야 합니다. 브라우저 자동 새로고침은 LiveReload 확장 기능이 필요하며, 일반 새로고침만으로도 사용할 수 있습니다. 배포할 때는 `dev` 프로필을 사용하지 않습니다.

## 구조

```text
src/main/java/com/barunsigong/interior/
  InteriorCmsApplication.java
  config/                    인증·프로필·초기 관리자
  common/                    공통 모델·오류·뷰 모델
  auth/                      계정·역할·로그인
  customer/                  고객 Controller / DTO / Service / Repository / Model
  project/                   현장 Controller / DTO / Service / Repository / Model
  dashboard/                 요약 화면
  survey/ estimate/ contract/ schedule/ work/
  payment/ completion/ material/ purchase/ cost/
  afterservice/ mobile/ notification/ setting/
  attachment/ audit/         파일 메타데이터·감사 이력 모델
src/main/resources/
  application*.yml            공통·Supabase·데모·운영 설정
  templates/                 화면별 Thymeleaf View, 공통 레이아웃
  static/css/ static/js/      반응형 스타일·공통 폼 동작
  db/migration/              Flyway 초기 스키마
  db/supabase/               Supabase API 접근 권한 제한
src/test/                    금액·실측·일정 계산 및 MVC/DB 통합 테스트
scripts/                     로컬 실행·테스트·빌드
docs/                        화면 매핑·구조·원본 텍스트
```

요청 흐름은 `Controller → Service → Repository → Model`이며, Controller가 `Model`에 화면 데이터를 담아 `templates`의 View를 반환합니다.
폼은 Entity에 직접 바인딩하지 않고 DTO로 검증합니다. 업무 저장과 업체 범위 검사는 Service에서 처리합니다.

## 테스트와 빌드

```powershell
.\scripts\run-local.ps1 -Task test
.\scripts\run-local.ps1 -Task package
```

테스트는 H2의 PostgreSQL 호환 모드에서 **공통 Flyway V1을 적용하고 JPA 매핑을 검증**합니다. Supabase 전용 V2 권한 설정은 실제 PostgreSQL에서 검증했습니다.
실제 Supabase 연결·마이그레이션·앱 시작 검증 결과는 [검증 기록](docs/verification.md)에 정리합니다.
패키징 결과: `target/interior-cms-0.0.1-SNAPSHOT.jar`.

서버 배포 시에는 HTTPS 뒤에서 `SPRING_PROFILES_ACTIVE=supabase,prod`를 사용하세요.
`prod`는 Secure 세션 쿠키를 켜므로 HTTP 로컬 테스트는 `supabase` 또는 `demo`로 실행합니다.

## 다음 개발

다른 장소의 개발자는 [Supabase DB 안내](docs/supabase-db.md)에 따라 연결합니다.
스키마 변경은 담당자가 먼저 적용하고, 다른 개발자는 `supabase,shared` 프로필로 실행할 수 있습니다.

[화면별 구현 범위와 순서](docs/screen-map.md), [아키텍처와 데이터 관계](docs/architecture.md)를 기준으로 이어서 개발할 수 있습니다.
첨부·PDF·견적 확정·수금·작업자 배정·상태 전이·감사 기록은 아직 운영 가능한 기능이 아닙니다.

로그인은 이메일 형식 검증 없이 아이디(`username`)로 처리하며 대소문자를 구분하지 않습니다. V3 마이그레이션은 기존 계정 식별자와 BCrypt 해시를 보존합니다. 기존 이메일 형태의 식별자는 그대로 아이디로 사용할 수 있습니다. 기존 로컬 설정 호환을 위해 ADMIN_EMAIL도 대체값으로 지원합니다. 새 설정에는 ADMIN_USERNAME을 사용하세요.
