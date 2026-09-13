CLAUDE.md

## 서비스: TYT (Take Your Time)
- "지금부터 몇 분 더 늦장 부려도 되는가"(여유)를 알려주는 서비스. `여유 = 도착 목표 시각 − 이동 시간 − 준비 시간 − 지금`
- MVP는 단발 일정 기반 개인 도구. 실시간 길찾기 API로 이동 시간을 구해 조회 시점에 여유를 계산한다. 반복 일정과 푸시 알림은 MVP 제외.
- 기획 상세(범위, 계산 정의, 도메인 분할, 미결정 사항): `docs/product.md`

## Project: tyt-be
- Spring Boot 4.0.x, Java 17, Gradle. 모듈: `tyt/`
- 빌드/테스트: `tyt/`에서 `./gradlew build`, `./gradlew test`
- base package: `com.tyt`
- 들여쓰기는 tab
- 도메인별 DDD 4계층, application·domain ↔ infrastructure는 반드시 DIP. 상세 규칙:
  - `.claude/rules/package-structure.md` (항상 로드)
  - `.claude/rules/api.md` (presentation 작업 시 로드)
  - `.claude/rules/testing.md` (테스트 파일 작업 시 로드. 새 테스트를 만들기 전에 먼저 읽을 것)
  - `.claude/rules/git-workflow.md` (항상 로드. issue → 브랜치 → PR → merge)

## 에이전트 문서 관리
- CLAUDE.md, `.claude/rules/`, `.claude/skills/`는 Claude가 작업 중 수시로 채운다. 사용자에게 따로 묻지 않는다.
  - 새 컨벤션을 사용자와 합의했을 때 → 해당 rules 파일
  - 같은 설명·수정 요청이 반복될 때 → rules
  - 같은 작업 절차가 반복될 때 → skill
- 합의된 내용과 코드에서 확인한 사실만 적는다. 추측성 규칙은 넣지 않는다.
- CLAUDE.md는 짧게 유지한다. 규칙은 rules, 절차는 skills로 보낸다.
- 갱신은 진행 중인 작업 브랜치에 함께 커밋하고, PR의 background에 무엇을 바꿨는지 적는다.

Tradeoff: These guidelines bias toward caution over speed. For trivial tasks, use judgment.

1. Think Before Coding
   Don't assume. Don't hide confusion. Surface tradeoffs.

Before implementing:

State your assumptions explicitly. If uncertain, ask.
If multiple interpretations exist, present them - don't pick silently.
If a simpler approach exists, say so. Push back when warranted.
If something is unclear, stop. Name what's confusing. Ask.
2. Simplicity First
   Minimum code that solves the problem. Nothing speculative.

No features beyond what was asked.
No abstractions for single-use code.
No "flexibility" or "configurability" that wasn't requested.
No error handling for impossible scenarios.
If you write 200 lines and it could be 50, rewrite it.
Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

3. Surgical Changes
   Touch only what you must. Clean up only your own mess.

When editing existing code:

Don't "improve" adjacent code, comments, or formatting.
Don't refactor things that aren't broken.
Match existing style, even if you'd do it differently.
If you notice unrelated dead code, mention it - don't delete it.
When your changes create orphans:

Remove imports/variables/functions that YOUR changes made unused.
Don't remove pre-existing dead code unless asked.
The test: Every changed line should trace directly to the user's request.

4. Goal-Driven Execution
   Define success criteria. Loop until verified.

Transform tasks into verifiable goals:

"Add validation" → "Write tests for invalid inputs, then make them pass"
"Fix the bug" → "Write a test that reproduces it, then make it pass"
"Refactor X" → "Ensure tests pass before and after"
For multi-step tasks, state a brief plan:

1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
   Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

These guidelines are working if: fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.