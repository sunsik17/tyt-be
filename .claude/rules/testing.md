---
paths:
  - "tyt/src/test/**/*.java"
---

# Testing

| 계층 | 방식 | 검증 대상 |
|---|---|---|
| domain | 순수 JUnit (Spring 없이) | Entity/VO 생성, 상태 변경, 불변식 위반 시 ErrorCode |
| application | JUnit + Mockito. repository·`port/out` 인터페이스를 mock | Service 흐름, 예외 분기 |
| presentation | `@WebMvcTest` + UseCase mock | 요청 검증, HTTP status, 응답 형식 |
| infrastructure | `@DataJpaTest` 등 슬라이스 테스트 | 커스텀 쿼리, adapter의 DTO 변환 |

- `@SpringBootTest`는 계층을 넘는 흐름 확인이 꼭 필요할 때만 쓴다.
- 성공 케이스뿐 아니라 예외 분기(어떤 ErrorCode가 나오는지)도 검증한다.
- 테스트 클래스는 대상 클래스와 같은 패키지에 둔다.
- Spring Boot 4는 슬라이스 테스트에 기술별 test 스타터가 필요하다 (예: `spring-boot-starter-webmvc-test`). 없으면 build.gradle에 추가한다.
