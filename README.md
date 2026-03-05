
# 403motors – 차량 견적/이벤트 기반 구성 서비스

Spring Boot 기반 **차량 구성 및 견적 저장 서비스**입니다.  
사용자는 트림/색상/옵션을 선택해 견적을 생성하고, 서버는 이벤트 정책을 적용해 **최종 금액을 재계산 후 저장**합니다.
CRUD게시판인 관리자 페이지는 데이터 삭제를 우려해 PPT PDF에서 확인 가능합니다.(관리자 계정 공개 X)

> ✅ AWS EC2 + Oracle Cloud DB 환경에 배포 완료
> 반드시 회원가입 진행하고 로그인 하시고 사용을 권장합니다.(견적 저장이 로그인 해야 가능)

## 📊 프로젝트 발표 자료

👉  [백엔드 PDF 바로 보기](./presentation/403motors.pdf)
---

## 🚀 실행 주소

### 🌐 Production (운영 서버)

- Web: https://www.403-motors.store/
- Swagger UI: https://www.403-motors.store/swagger-ui/index.html
- OpenAPI JSON: https://www.403-motors.store/v3/api-docs

### 🖥 Local

- Web: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

---

## ☁️ 배포 환경

| 구성 요소 | 사용 기술 |
|------------|------------|
| Application Server | AWS EC2 |
| Database | Oracle Cloud |
| Backend | Spring Boot 3 / Java 21 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security |
| Template Engine | Thymeleaf |
| API Documentation | springdoc-openapi (Swagger) |

> 운영 환경에서는 `prod` 프로파일 기준으로 실행됩니다.  
> DB 및 OAuth 관련 민감 정보는 환경 변수로 관리합니다.

---

## 🔐 로그인 정책 안내

### 운영 서버

- 일반 회원가입 / 로그인 가능
- 관리자 계정은 데이터 보호를 위해 비공개합니다.
- **소셜 로그인(OAuth)은 비활성화 상태**

### 로컬 환경

각자 발급받은 OAuth Client 정보를 `application.yml` 또는 환경 변수에 설정하면 소셜 로그인 사용 가능.

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_CLIENT_ID
            client-secret: YOUR_CLIENT_SECRET
            redirect-uri: "{baseUrl}/login/oauth2/code/google"
```

> 🔒 OAuth Client Secret은 보안상 운영 서버에 포함하지 않았습니다.

---

## 🔐 데모 계정

### 사용자
- 회원가입 후 사용

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

- 서버에서 **최종 가격 재계산 후 저장**
- 트랜잭션 기반 견적 저장 (부분 실패 시 전체 롤백)
- 이벤트 정책 자동 매칭 및 최적 할인 선택
- 표준화된 API 에러 응답 (ErrorResponse)
- Swagger 기반 API 문서 자동 생성

> 요청에 포함된 `totalPrice` 값은 신뢰하지 않으며,  
> 서버에서 정책 적용 후 최종 금액을 다시 계산합니다.

---

## 📡 핵심 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/quotes` | 견적 생성 |
| GET | `/api/trims/{trimId}` | 트림 상세 조회 |
| GET | `/api/models/{modelId}/likes` | 좋아요 상태 조회 |
| POST | `/api/models/{modelId}/likes` | 좋아요 추가 |
| DELETE | `/api/models/{modelId}/likes` | 좋아요 취소 |
| GET | `/health` | 서버 상태 확인 |
| GET | `/db-check` | DB 연결 확인 |

자세한 명세는 Swagger 문서를 참고하세요.

👉 https://www.403-motors.store/swagger-ui/index.html

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

- 400: 잘못된 요청
- 404: 리소스 없음
- 500: 서버 오류

---

## 🛠 로컬 실행

### 요구사항

- Java 21+
- Gradle
- Oracle Database

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

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA (Hibernate)
- Oracle Database
- Thymeleaf
- Swagger / OpenAPI (springdoc)

---

## ⚠ 제한사항

- Swagger는 시연 및 API 구조 설명을 위해 운영 서버에 공개되어 있습니다.
- 실서비스 환경에서는 Swagger 비활성화 또는 접근 제한이 필요합니다.
- `/db-check` 엔드포인트는 운영 점검용입니다.
- OAuth는 보안상 운영 서버에서 비활성화되어 있습니다.

---

## 📌 향후 개선 계획

- Swagger 운영 환경 접근 제한
- `/db-check` 운영 환경 비활성화
- CI/CD 파이프라인 구축
- 통합 테스트 및 API 테스트 강화
- 모니터링 및 로깅 체계 개선
- OAuth 운영 환경 분리 적용
