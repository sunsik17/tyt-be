# Git workflow

main에 직접 커밋하지 않는다. 모든 작업은 아래 순서로 진행한다.

1. 작업 시작 전 GitHub issue를 만든다 (`gh issue create`).
2. main을 최신화하고 브랜치를 만든다: `<prefix>/<issue번호>-<작업-이름>` (예: `feat/12-matching-create`)
   - prefix: `feat`(기능), `fix`(버그), `refactor`(리팩터링), `test`(테스트), `docs`(문서), `chore`(빌드·설정)
   - 작업 이름은 영문 kebab-case
3. 작업을 커밋하고 브랜치를 push한다.
4. PR을 만든다. 본문은 `.github/pull_request_template.md` 형식(summary / change / background or etc / test)을 따른다.
   - summary에 `Closes #<issue번호>`를 넣어 머지 시 issue가 자동으로 닫히게 한다.
   - test에는 실제로 실행한 검증(명령어와 결과)만 적는다.
5. squash merge 후 원격·로컬 브랜치를 삭제하고 로컬 main을 최신화한다.
