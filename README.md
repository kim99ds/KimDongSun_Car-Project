# CAR_PROJECT - Spring Boot Starter (Oracle XE)

팀 배포용 기본 템플릿입니다. (JDK 21 고정 / Gradle / Spring Data JPA / Oracle)

## 1) 요구사항
- JDK 설치: 17/21/25 중 아무거나 가능 (빌드/컴파일은 Gradle toolchain으로 21 사용)
- Oracle XE (대부분 서비스명: XE, 팀원 1명만 XEPDB1)

## 2) DB 접속 (기본값)
- username: car_project
- password: 1234
- host: localhost
- port: 1521
- service: XE

서비스명이 XEPDB1(또는 PDB1)인 팀원은 환경변수만 바꿔서 실행하면 됩니다.

### Windows (PowerShell)
$env:ORACLE_SERVICE="XEPDB1"   # 또는 PDB1
./gradlew bootRun

### macOS/Linux
ORACLE_SERVICE=XEPDB1 ./gradlew bootRun

## 2-1) (선택) URL 전체를 통째로 바꾸고 싶으면
application.yml은 ORACLE_URL을 제공하면 그 값을 우선 사용합니다.

예) XEPDB1을 URL로 직접 지정
- Windows: $env:ORACLE_URL="jdbc:oracle:thin:@//localhost:1521/XEPDB1"
- macOS/Linux: ORACLE_URL=jdbc:oracle:thin:@//localhost:1521/XEPDB1

## 3) 프로필
- 기본: ddl-auto=none (DDL을 존중)
- 개발 편의: `--spring.profiles.active=dev` (ddl-auto=update)

예)
./gradlew bootRun --args="--spring.profiles.active=dev"

## 4) 패키지 구조
- car: 브랜드/모델/트림/색상/이미지
- option: 옵션/트림옵션
- member: 회원/권한
- event: 이벤트/대상/정책
- quote: 견적/견적옵션/견적이벤트

## 5) 엔티티 설계 포인트
- MEMBER_ROLE: 복합키(EmbeddedId + MapsId)
- QUOTE -> TRIM_COLOR: DB는 (TRIM_ID, TRIM_COLOR_ID) 복합 FK이므로,
  Quote 엔티티에는 trimColorId 컬럼을 두고, trimColor 연관관계는 read-only로 매핑했습니다.
  (저장은 trim + trimColorId로 처리)
