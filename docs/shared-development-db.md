# VPN 공유 개발 DB

## 현재 준비 상태 (2026-09-23)

- Tailscale 설치·로그인 완료. DB 호스트의 VPN IPv4: `100.87.143.100`.
- `interior_cms` DB에 Flyway V1 적용 완료 (업무 테이블 23개 + 이력 테이블 1개).
- 개발용 DB 계정 생성 및 MySQL TLS 접속 확인 완료.
- 개발 계정으로 `mysql,shared` 앱 시작, JPA 스키마 검증 및 로그인 페이지 HTTP 200 확인 완료.
- 최초 웹 관리자 계정 생성 완료. 비밀번호는 호스트의 `.env`에서만 확인합니다.
- 원격 공유는 비활성 상태입니다 (`tailscale serve status`: `No serve config`).
- 개발자 추가, 접근 정책 설정, TCP 13306 전달 활성화와 외부 접속 검증은 나중에 진행합니다.
- `.tools/shared-db.env`에는 추후 전달할 연결 정보가 준비되어 있지만 현재 VPN 주소로 DB 접속은 열리지 않았습니다.

## 구성

개발자 PC → Tailscale VPN → DB 호스트의 TCP 13306 → 로컬 MySQL 3306.

DB 호스트는 현재 개발 PC입니다. PC가 꺼지거나 절전 상태이거나 VPN이 끊기면 접속할 수 없습니다.
Tailscale Serve의 TCP 전달 기능을 사용하며 공개 인터넷용 Funnel은 사용하지 않습니다.
VPN 접근 정책에서 승인된 개발자에게 호스트의 TCP 13306만 허용해야 합니다.
기본 전체 허용 정책이 있으면 제한 규칙을 추가하는 것만으로 접근이 제한되지 않습니다.
VPN 로그인, 접근 정책, 상대 기기 연결 확인이 끝나기 전에는 원격 공유 완료 상태가 아닙니다.

## 개발자 설정

1. DB 관리자가 승인한 Tailscale 네트워크/공유 기기에 연결합니다.
2. JDK 25를 설치하고 프로젝트를 받습니다.
3. 프로젝트 루트에 `.env`를 만들고 관리자가 전달한 주소와 계정을 입력합니다.

```dotenv
DB_URL=jdbc:mysql://VPN_HOST:13306/interior_cms?sslMode=REQUIRED&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&characterEncoding=UTF-8
DB_USERNAME=interior_dev
DB_PASSWORD=관리자가_별도로_전달한_비밀번호
```

4. IntelliJ의 `Interior CMS Shared DB` 설정으로 실행하거나 아래 명령을 사용합니다.

```powershell
.\scripts\run-local.ps1 -Profile 'mysql,shared'
```

macOS/Linux에서는 JDK 25와 실행 권한을 준비한 뒤 프로젝트 루트에서 실행합니다.

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql,shared
```

DataGrip에서는 VPN 호스트, 포트 13306, 데이터베이스 `interior_cms`와 전달받은 DB 계정을 사용합니다.
MySQL 연결의 SSL mode를 REQUIRED로 설정합니다. 이 설정은 암호화를 요구하지만 인증서 신원을 검증하지는 않습니다.
접속할 서버의 식별과 네트워크 접근 제한은 승인된 VPN 기기와 접근 정책을 통해 관리합니다.

## 권한 및 스키마 변경

- `interior_dev@localhost`는 TCP 전달기가 MySQL에 로컬로 연결할 때 사용하는 개발 계정입니다.
- 이 계정에는 `interior_cms`의 SELECT, INSERT, UPDATE, DELETE만 부여합니다. 데이터 삭제는 가능하므로 개발용 데이터만 사용합니다.
- `mysql,shared` 프로필은 Flyway 실행을 끄고 JPA 스키마 검증만 수행합니다.
- 테이블 변경은 DB 관리자가 검토한 Flyway 마이그레이션을 별도 권한으로 적용합니다.
- 적용된 V1 파일을 수정하지 말고 V2 이후 파일로 변경을 추가합니다.
- 공유 DB에서 `demo`, `create`, `create-drop` 설정을 사용하지 않습니다.
- 최초 웹 관리자 생성은 DB 관리자가 진행합니다. DB 로그인과 웹 로그인은 별개입니다.
- 여러 개발자를 등록할 때는 개발자별 DB 계정을 발급하여 개별 회수가 가능하게 합니다.

## DB 호스트 명령

VPN 접근 정책을 설정한 후 관리자 PowerShell에서 실행합니다.

```powershell
& 'C:/Program Files/Tailscale/tailscale.exe' serve --bg --tcp=13306 tcp://127.0.0.1:3306
& 'C:/Program Files/Tailscale/tailscale.exe' serve status
```

공유 중지:

```powershell
& 'C:/Program Files/Tailscale/tailscale.exe' serve --tcp=13306 off
```

비밀번호가 든 `.env`와 `.tools/shared-db.env`는 Git에서 제외합니다.
외부 개발자에게 root 비밀번호나 최초 웹 관리자 비밀번호를 전달하지 않습니다.
개발자가 연결한 뒤 SELECT 조회와 앱 실행을 확인해야 원격 검증이 완료됩니다.
