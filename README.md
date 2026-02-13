# 403motors – 차량 견적/이벤트 기반 구성 서비스

Spring Boot 기반 **차량 구성 및 견적 저장 서비스**입니다.
사용자는 트림/색상/옵션을 선택해 견적을 생성하고, 서버는 이벤트 정책을 적용해 **최종 금액을 재계산 후 저장**합니다.

> ⚠ 현재는 로컬 실행 기준이며, AWS 배포 예정입니다.

## 📊 프로젝트 발표 자료

👉  [백엔드 PDF 바로 보기](./presentation/403motors.pdf)
👉  [백엔드+프론트 합본 PDF 바로 보기](./presentation/403motors_합본.pdf)
---

## 🚀 실행 주소 (로컬 기준)

* Web: http://localhost:8080
* API Base: http://localhost:8080/api
* Swagger UI: http://localhost:8080/swagger-ui/index.html
* OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## 🔐 데모 계정

### 사용자

* ID: `{DEMO_USER_ID}`
* PW: `{DEMO_USER_PW}`

### 관리자

* ID: `{DEMO_ADMIN_ID}`
* PW: `{DEMO_ADMIN_PW}`

---

## 🎬 데모 시연 순서 (약 3분)

1. 로그인 → 차량 모델 선택
2. 트림/색상 선택 후 견적 생성
3. 옵션 추가/제거 → 금액 변화 확인
4. 이벤트 할인 자동 적용 확인
5. 견적 저장 성공 확인

> 여러 이벤트가 매칭될 경우 **가장 유리한 1개만 적용**됩니다.

---

## ⭐ 핵심 기능

* 서버에서 **최종 가격 재계산 후 저장**
* 트랜잭션 기반 견적 저장 (부분 실패 시 전체 롤백)
* 이벤트 정책 자동 매칭 및 최적 할인 선택
* 표준화된 API 에러 응답 (ErrorResponse)
* Swagger 기반 API 문서 자동 생성

> 요청에 포함된 `totalPrice` 값은 신뢰하지 않으며,
> 서버에서 정책 적용 후 최종 금액을 다시 계산합니다.

---

## 📡 핵심 API

| Method | Endpoint                      | 설명        |
| ------ | ----------------------------- | --------- |
| POST   | `/api/quotes`                 | 견적 생성     |
| GET    | `/api/trims/{trimId}`         | 트림 상세 조회  |
| GET    | `/api/models/{modelId}/likes` | 좋아요 상태 조회 |
| POST   | `/api/models/{modelId}/likes` | 좋아요 추가    |
| DELETE | `/api/models/{modelId}/likes` | 좋아요 취소    |
| GET    | `/health`                     | 서버 상태 확인  |
| GET    | `/db-check`                   | DB 연결 확인  |

자세한 명세는 Swagger 문서를 참고하세요.

👉 `/swagger-ui/index.html`

---

## 🧾 API 예시

### 견적 생성

POST `/api/quotes`

```json
{
  "modelId": 80004,
  "variantId": 12345,
  "trimId": 123,
  "trimColorId": 456,
  "packageOptionIds": [111, 222],
  "singleOptionIds": [333, 444]
}
```

→ 서버에서 가격 계산 및 이벤트 적용 후 견적 저장

---

## ❗ 에러 응답 형식

모든 API는 공통 `ErrorResponse` 포맷을 사용합니다.

```json
{
  "code": "INVALID_ARGUMENT",
  "message": "옵션이 허용되지 않습니다.",
  "path": "/api/quotes",
  "timestamp": "2026-02-13T00:00:00+09:00"
}
```

### 상태 코드

* 400: 잘못된 요청
* 404: 리소스 없음
* 500: 서버 오류

---

## 🛠 로컬 실행

### 요구사항

* Java 21+
* Gradle
* Oracle Database

### 환경 변수 예시

```bash
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521:XE
SPRING_DATASOURCE_USERNAME=car_project
SPRING_DATASOURCE_PASSWORD=******
```

### 실행

```bash
./gradlew clean bootRun
```

---

## 🏗 기술 스택

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA (Hibernate)
* Oracle DB
* Thymeleaf
* Swagger / OpenAPI (springdoc)

---

## ⚠ 제한사항

* OAuth 리다이렉트는 배포 도메인 기준
* 견적 데이터는 사용자 액션 기반 생성
* Swagger는 개발 환경에서만 활성화 예정

---

## 📌 TODO (향후 개선)

* AWS 배포 및 운영 모니터링 설정
* 통합 테스트 추가
* API 성능 로깅 및 메트릭 수집

---
