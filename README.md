# PickitPickit_BE
삐끼삐끼 백엔드 레포지토리입니다.


### 📌 협업 규칙
모든 기능 개발은 다음 흐름을 따릅니다:
1. 개발하고자 하는 기능에 대한 이슈를 등록하여 번호를 발급합니다.
2. dev 브랜치로부터 분기하여 이슈 번호를 사용해 이름을 붙인 feat 브랜치를 만든 후 작업합니다.
3. 작업이 완료되면 dev 브랜치에 풀 요청(Pull Request)을 작성하고, 팀원의 동의를 얻으면 병합합니다.
---

1.  초기 세팅
* 원하는 로컬 경로에 클론
    ```
    $ git clone git@github.com:Gyeonggi-Festa/GyeonggiFesta_BE.git
    ```

2. Git Flow 브랜치 전략
* [`main(배포)` - `dev(통합)` - `feat(개발)·fix(수정)`] 세 단계
* 개발 완료 시 `PR`로 `dev`에 병합 → 주기적으로 `release/x.y`로 묶어 `QA` 후 `main`에 병합

* 브랜치 네이밍:
  `feat/#<이슈 번호>-<간단한 설명> # 예시: feat/#1-google-login`
3. 기능 개발
    ```bash
    # 1️⃣ 최신 통합 브랜치 받기
    $ git checkout dev
    $ git pull origin dev
    
    # 2️⃣ 기능 브랜치 생성
    $ git checkout -b feat/#<이슈 번호>-<간단 설명>
    
    # 3️⃣ 작업 & 단위 커밋
    $ git commit -m "feat: kakao oauth callback (#<이슈 번호>)"
   ```
* 커밋 규칙: `type: subject`
    * `feat`|`fix`|`docs`|`refactor`|`test`|`chore`|`rename`

4. PR 작성 및 리뷰 규칙
* PR을 생성해서 양식에 맞게 작성
    * `feat: 일반 로그인 API 구현`
* 개발 중이라도 일찍 공유해 피드백 주고받기
    * 제안(코드 `suggestion`)→ 대안 함께 제시하기
* 파일 `10`개 미만으로 올리기
* 리뷰어 최소 `1`명 `Approve` → `Merge commit` 방식으로 병합
6. dev 최신화 & 추가 작업
    ```bash
    $ git checkout dev
    $ git pull origin dev            # dev 최신 상태 반영
    $ git branch -d feat/<name>      # 로컬 브랜치 정리
   ```
* 새 기능 구현 시 항상 최신 dev 기준으로 새 브랜치를 파서 충돌 최소화.
---

### 🖥️ 코드 컨벤션
> 운영 언어: Java21  
> 빌드 도구: Gradle  
> 프레임워크: Spring Boot 3.x

1. 네이밍
   * 주로 `camelCase`
   * `Boolean` 메서드는 `is/has/can` 패턴 사용—예: `isActive()`, `hasRole()`.
2. 형식
    * 들여쓰기: `4 spaces` = `tap`
    * 행 길이: `120`자 제한
    * 주석: `//`→ 짧은 설명, `/* … */`→ 블록
3. 메서드, 매개변수
    * CRUD 접두어: `create, retrieve, update, delete`
4. 엔티티
    * `@Table(name = "users")`등 명시적 테이블명 지정. 예약어는 복수형으로 회피(`order` → `orders`).
    * `@Column(nullable = false)`를 필수 필드에만 기재
    * 시간 컬럼은 BaseEntity 상속받아 사용
5. DTO & 매핑
   * 변환 책임을 DTO로 위임할 것
   * DTO 네이밍: `UserCreateRequest`, `UserSummaryResponse`, `PaymentClientResponse` -dto 금지
6. Validation
   * Controller 단에서 `@Valid` 활용
7. 메서드 순서
   * `public` 맨 뒤 `private` 메서드 넣을 것
8. 어노테이션 정렬
   * 스프링 핵심(`@Service`, `@Configuration`)
   * 트랜잭션 보안
   * 검증/로그
   * Lombok (`@Getter`, `@Builder`) - 가장 하단
10. 제어 흐름 & 복잡도
    * 중첩 `depth`는 `2`이하, `early return`
      * 불필요한 `else`는 제거하기
---

### 🏛️ 프로젝트 구조
```text
 Pickitpickit
	member // entity 기준으로 패키지 분리
	- controller
	- service
	- dto
	- entity
	- repository
	- exception
  util
	- jwt
	- response
  - security
```

### ⬇️ ERD

---

### 🗄️ DB 백업/복구 (간단)
상세 운영 가이드는 [`docs/db-operations-guide.md`](docs/db-operations-guide.md)를 참고하세요.

1. 백업 (`pg_dump`, custom format)
```bash
mkdir -p backups # backups 폴더 없다면
docker compose exec -T postgres pg_dump -U pickit -d pickit -Fc \
  > backups/pickit_$(date +%Y%m%d_%H%M%S).dump
```

2. 복구 (`pg_restore`)
```bash
# 앱(bootRun 등) 먼저 종료

# DB 컨테이너는 켜둠
docker compose up -d postgres
# 복구 전 세션 정리 
docker compose exec -T postgres psql -U pickit -d postgres -c "
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'pickit'
  AND pid <> pg_backend_pid();
"

# DB 재생성
docker compose exec -T postgres dropdb -U pickit --if-exists pickit
docker compose exec -T postgres createdb -U pickit pickit

# 덤프 복원
docker compose exec -T postgres pg_restore -U pickit -d pickit --no-owner --no-privileges \
  < backups/<backup-file>.dump # <backup-file>.dump 부분에 백업 파일명 입력
```

3. dump 파일 공유 원칙
* dump 파일은 Git에 커밋하지 않습니다.
* 접근제어된 팀 스토리지(팀 드라이브/private bucket)에만 업로드합니다.
* 팀 채널에는 `파일명`, `생성시각(KST)`, `SHA256`, `복원가이드 링크`만 공유합니다.

4. 참고
* 운영에서는 `docker compose down -v` / `docker volume prune` 실행 전에 반드시 백업하세요.
* Flyway는 스키마 버전 관리, `pg_dump`/`pg_restore`는 운영 데이터 보존/복구 용도입니다.
