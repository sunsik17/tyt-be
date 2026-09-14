---
paths:
  - "tyt/src/main/java/**/presentation/**/*.java"
---

# API

## URL

- 클라이언트용: `/api/v1/<resources>`. 리소스는 복수형 kebab-case 명사 (`/api/v1/user-settings`).
- 서비스 간 내부 호출용: `/internal/v1/<resources>`. 외부에 노출하지 않는다.
- 동사 대신 하위 리소스로 표현한다: `PATCH /api/v1/matchings/{id}/approval`
- prefix를 두는 이유:
  - `/api`: 이후 생길 수 있는 SSR 페이지, 외부 연동(webhook 등), actuator·문서 경로와 구분하고, 보안·CORS·게이트웨이 라우팅을 prefix 하나로 적용하기 위해
  - `/v1`: 강제 업데이트가 어려운 클라이언트(앱)를 v1에 둔 채 v2를 병행 배포하기 위해. 버전은 breaking change가 있을 때만 올린다
  - `/internal`: 도메인이 서비스로 분리될 때 도메인 간 호출 경로가 되며, 외부 API와 인증·권한 정책을 분리하기 위해

## Controller

- 사용자용 Controller는 같은 도메인의 Service에 의존한다.
- `/internal/v1` Controller(서비스 분리 후)는 `application/port/in`의 UseCase에 의존한다.
- 반환 타입은 `ResponseEntity<ApiResponse<XxxResponse>>`. Entity나 Result를 그대로 반환하지 않는다.
- 요청은 `@Valid` + Request record의 Bean Validation으로 형식만 검증한다. 비즈니스 규칙 검증은 domain에서 한다.
- 인증 주체는 `@AuthenticationPrincipal Long userId`로 받는다. JWT 필터가 principal에 userId(Long)를 넣는다.
  - 공개 경로(`/api/v1/auth/**`)에서는 principal이 `"anonymousUser"`라 null이 들어온다. 타입이 맞지 않아도 예외 없이 null이므로 공개 경로에서는 쓰지 않는다.
  - principal을 Long으로 두는 이유: 전용 타입을 auth에 두면 다른 도메인 컨트롤러가 auth를 import하게 된다. 역할 등 담을 정보가 늘면 principal 타입을 `common`에 둔다.

## 응답과 예외

- 성공: `ApiResponse.success(SuccessCode, data)`. 도메인 전용 성공 코드는 `presentation/dto/response/constants/<Domain>SuccessCode`.
- 실패: `BusinessException(<Domain>ErrorCode)`를 던지고, `common.web`의 전역 예외 처리에서 `ApiResponse.error(...)`로 변환한다.
- `<Domain>ErrorCode`는 `domain/exception`에 두고 `ErrorCode`를 구현하며, `ErrorType`과 메시지를 가진다. `ErrorType` → `HttpStatus` 변환은 전역 예외 처리에서만 한다.
- 특정 Controller 전용 처리가 필요할 때만 `presentation/errorhandler`에 `@RestControllerAdvice(assignableTypes = XxxController.class)`를 둔다.
