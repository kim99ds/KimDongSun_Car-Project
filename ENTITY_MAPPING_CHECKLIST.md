# CAR_PROJECT 엔티티 매핑 체크리스트

이 프로젝트는 Oracle 스키마 **CAR_PROJECT** 기준이며, 아래 테이블이 **전부 엔티티로 구현**되어 있습니다.

| TABLE | Entity 파일 |
|---|---|
| `BRAND` | `src/main/java/com/carproject/car/entity/Brand.java` |
| `CAR_MODEL` | `src/main/java/com/carproject/car/entity/CarModel.java` |
| `CAR_VARIANT` | `src/main/java/com/carproject/car/entity/CarVariant.java` |
| `CAR_TRIM` | `src/main/java/com/carproject/car/entity/CarTrim.java` |
| `CAR_COLOR` | `src/main/java/com/carproject/car/entity/CarColor.java` |
| `TRIM_COLOR` | `src/main/java/com/carproject/car/entity/TrimColor.java` |
| `CAR_IMAGE` | `src/main/java/com/carproject/car/entity/CarImage.java` |
| `OPTION_ITEM` | `src/main/java/com/carproject/car/entity/OptionItem.java` |
| `TRIM_OPTION` | `src/main/java/com/carproject/car/entity/TrimOption.java` |
| `MEMBER` | `src/main/java/com/carproject/member/entity/Member.java` |
| `ROLE` | `src/main/java/com/carproject/member/entity/Role.java` |
| `MEMBER_ROLE` | `src/main/java/com/carproject/member/entity/MemberRole.java` |
| `EVENT` | `src/main/java/com/carproject/event/entity/Event.java` |
| `EVENT_POLICY` | `src/main/java/com/carproject/event/entity/EventPolicy.java` |
| `EVENT_TARGET` | `src/main/java/com/carproject/event/entity/EventTarget.java` |
| `QUOTE` | `src/main/java/com/carproject/quote/entity/Quote.java` |
| `QUOTE_OPTION` | `src/main/java/com/carproject/quote/entity/QuoteOption.java` |
| `QUOTE_EVENT` | `src/main/java/com/carproject/quote/entity/QuoteEvent.java` |

## DB 서비스명
- 기본: `XE`
- XEPDB1 사용자: 실행 시 `ORACLE_SERVICE=XEPDB1` 설정

## JDK
- Gradle toolchain: **21** 고정

