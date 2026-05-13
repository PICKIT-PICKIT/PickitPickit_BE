# PickitPickit_BE Agent Guide

## Basics
- Use `AGENTS.md` for Codex project instructions.
- Follow `README.md` for collaboration, Git Flow, PR, and code conventions.
- Prefer small, focused changes. Do not revert user or teammate changes.

## Environment
- Java 21, Gradle, Spring Boot 3.x.
- Local DB is PostgreSQL via Docker.
- User usually runs/builds in IntelliJ and inspects DB in DataGrip.
- Codex should modify code, inspect repo state, and run focused checks when useful.

## Secrets
- Do not commit `.env` or `src/main/resources/application.yml`.
- Keep real API keys, passwords, and tokens outside Git.
- `application.yml` should reference env vars, e.g. `${KAKAO_API_KEY}`.
- Do not commit `.DS_Store`, `.codex/`, dump files, or DB backups.

## Git
- Main branches: `main`, `dev`.
- Feature branches are created from latest `dev`.
- Branch format: `feat/#<issue>-<description>`.
- Quote branch names containing `#`: `git switch --track 'origin/feat/#8-auth'`.
- Commit format: `type: subject (#<issue>)`.
- Allowed types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `rename`.

## PR
- Target `dev` for feature PRs.
- Follow `.github/PULL_REQUEST_TEMPLATE.md`.
- PR title style: `[FEAT] 기능 설명(#이슈번호)`.
- Before PR, prefer `./gradlew clean build` when feasible.

## Checks
- Compile: `./gradlew compileJava`
- Test: `./gradlew test`
- Full build: `./gradlew clean build`
- If a check cannot be run, say why in the final response.

## DB
- Follow `docs/db-operations-guide.md` for backup/restore.
- Do not edit already-applied Flyway migrations; add a new migration instead.
