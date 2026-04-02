# PickitPickit_BE DB 운영 가이드 (Flyway + PostgreSQL)

작성 시점: 2026-04-03 (KST)
대상: 로컬 개발/테스트 및 배포 후 운영 환경

## 0. 바로 쓰는 백업/복구 (팀 공통)

아래 순서만 그대로 실행하면 됩니다.

1. DB 컨테이너 실행
```bash
docker compose up -d postgres
docker compose ps postgres
```

2. 백업 생성
```bash
mkdir -p backups
docker compose exec -T postgres pg_dump -U pickit -d pickit -Fc \
  > backups/pickit_$(date +%Y%m%d_%H%M%S).dump
```

3. 복구 (중요: 앱 먼저 종료)
```bash
# bootRun 등 DB 붙는 프로세스가 있으면 먼저 종료

# (A) 기존 접속 세션 종료
docker compose exec -T postgres psql -U pickit -d postgres -c "
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'pickit'
  AND pid <> pg_backend_pid();
"

# (B) DB 재생성
docker compose exec -T postgres dropdb -U pickit --if-exists pickit
docker compose exec -T postgres createdb -U pickit pickit

# (C) dump 복원
docker compose exec -T postgres pg_restore -U pickit -d pickit --no-owner --no-privileges \
  < backups/<backup-file>.dump # <backup-file>.dump 부분에 백업 파일명 입력 
```

4. 복구 확인
```bash
docker compose exec -T postgres psql -U pickit -d pickit -c "select count(*) from stores;"
docker compose exec -T postgres psql -U pickit -d pickit -c "select count(*) from flyway_schema_history;"
```

## 1. dump 파일 팀 공유 방법

1. Git에는 올리지 않습니다.
- `.gitignore`에 `backups/*`, `*.dump`가 이미 포함되어 있습니다.
- 폴더만 공유하기 위해 `backups/.gitkeep`만 추적합니다.

2. 공유 위치
- 사내 공유 드라이브/클라우드의 접근제어된 폴더(예: 팀 전용 드라이브, private bucket)에 업로드합니다.
- 공개 링크/공개 저장소 업로드는 금지합니다.

3. 팀 채널 공유 템플릿
- 파일명: `pickit_YYYYMMDD_HHMMSS.dump`
- 해시: `sha256sum <file>`
- 기준 시각(KST): 백업 생성 시간
- 복원 명령: 이 문서 `0. 바로 쓰는 백업/복구`의 복원 블록 링크

공유 전 해시 생성:
```bash
sha256sum backups/<backup-file>.dump
```

권장 운영:
1. 최신 dump 1개 + 직전 안정 dump 1개를 최소 보관
2. 복구 리허설 성공한 dump 파일에만 "복구확인" 표시

예시 메시지:
```text
[DB 백업 공유]
- 파일명: pickit_20260403_030307.dump
- 생성시각(KST): 2026-04-03 03:03
- SHA256: b3c7216eb7517a97a07b63afd6114de7be7aca20b3e0482f0c38fa33c05c6fba
- 복원가이드: docs/db-operations-guide.md
```

## 2. 왜 이렇게 운영하는가

1. Flyway는 스키마 버전 관리용입니다.
- 테이블/인덱스/제약조건 같은 구조 변경은 Flyway migration으로 관리합니다.

2. 운영 데이터는 백업/복구로 보존합니다.
- `stores`, `users`, `search_logs` 같은 데이터는 dump 파일과 스토리지 보관 정책으로 보호합니다.

3. 이미 적용된 마이그레이션은 수정하지 않습니다.
- `V__` 파일은 새 버전을 추가하는 방식으로만 전진합니다.

## 3. 개발/배포 시 운영 체크

1. 개발 단계
- 앱 실행 시 Flyway 자동 적용 (`spring.flyway.locations=classpath:db/migration`)
- `spring.jpa.hibernate.ddl-auto=validate` 기준 유지
- `docker compose down -v` 또는 `docker volume prune` 실행 전 백업

2. 배포 단계
- 배포 전: 백업 먼저 수행
- 배포 중: Flyway migrate 실패 시 즉시 중단
- 배포 후: `flyway_schema_history`, 핵심 테이블 row count 확인

## 4. 장애 대비 체크리스트

1. 백업 주기: 최소 일 1회
2. 보관 위치: 로컬 외 별도 스토리지 필수
3. 복구 리허설: 최소 월 1회
4. 팀 합의: RTO/RPO 기준 명시
