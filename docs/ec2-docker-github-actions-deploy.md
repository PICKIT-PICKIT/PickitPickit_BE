# EC2 Docker GitHub Actions 배포 가이드

이 문서는 새 EC2 인스턴스에 PickitPickit Spring Boot API를 Docker로 실행하고, GitHub Actions로 자동 배포하는 기준 절차입니다.

## 1. AWS 구성

권장 구성:

```text
사용자
-> pickit-pickit.site
-> EC2 Nginx
-> Docker container: Spring Boot API
-> RDS PostgreSQL
```

보안 그룹:

- EC2 inbound: `22`는 본인 IP만 허용, `80`, `443`은 전체 허용
- RDS inbound: PostgreSQL `5432`를 EC2 보안 그룹에서만 허용
- EC2 outbound: 기본 허용

## 2. 보안 그룹 설정

보안 그룹은 "어떤 트래픽을 받을지" 정하는 방화벽입니다. EC2와 RDS는 보안 그룹을 분리해서 관리하는 것을 권장합니다.

### 2.1 EC2 보안 그룹

EC2 인스턴스에 연결할 보안 그룹입니다. 예: `pickitpickit-ec2-sg`

AWS Console:

```text
EC2 -> 인스턴스 -> 대상 인스턴스 선택
-> 보안 탭
-> 보안 그룹 클릭
-> 인바운드 규칙 편집
```

인바운드 규칙:

| 유형 | 프로토콜 | 포트 범위 | 소스 | 설명 |
| --- | --- | --- | --- | --- |
| SSH | TCP | 22 | 내 IP | EC2 접속용 |
| HTTP | TCP | 80 | Anywhere-IPv4 `0.0.0.0/0` | Certbot 인증 및 HTTP 접속 |
| HTTPS | TCP | 443 | Anywhere-IPv4 `0.0.0.0/0` | 실제 서비스 접속 |

선택 사항:

| 유형 | 프로토콜 | 포트 범위 | 소스 | 설명 |
| --- | --- | --- | --- | --- |
| HTTP | TCP | 80 | Anywhere-IPv6 `::/0` | IPv6를 쓸 때만 |
| HTTPS | TCP | 443 | Anywhere-IPv6 `::/0` | IPv6를 쓸 때만 |

주의:

- `22`번 SSH를 `0.0.0.0/0`으로 열지 않습니다.
- Spring Boot 포트 `8080`은 외부에 열지 않습니다.
- `docker-compose.prod.yml`에서 `127.0.0.1:8080:8080`으로 묶기 때문에 Nginx만 앱에 접근합니다.
- 스크린샷처럼 "모든 트래픽"을 다른 보안 그룹에서 허용하는 규칙은 임시 테스트가 아니라면 권장하지 않습니다.

### 2.2 RDS 보안 그룹

RDS PostgreSQL에 연결할 보안 그룹입니다. 예: `pickitpickit-rds-sg`

AWS Console:

```text
RDS -> 데이터베이스 -> 대상 DB 선택
-> 연결 & 보안
-> VPC 보안 그룹 클릭
-> 인바운드 규칙 편집
```

인바운드 규칙:

| 유형 | 프로토콜 | 포트 범위 | 소스 | 설명 |
| --- | --- | --- | --- | --- |
| PostgreSQL | TCP | 5432 | EC2 보안 그룹 ID | EC2의 Spring Boot만 DB 접속 허용 |

소스에는 IP가 아니라 EC2 보안 그룹 ID를 넣습니다.

예:

```text
sg-0b217a5c4aa3eb1ee
```

이렇게 하면 EC2의 public IP가 바뀌어도 같은 보안 그룹에 속한 EC2는 RDS에 계속 접속할 수 있습니다.

로컬 DataGrip에서 직접 RDS에 붙어야 하는 경우만 임시로 아래 규칙을 추가합니다.

| 유형 | 프로토콜 | 포트 범위 | 소스 | 설명 |
| --- | --- | --- | --- | --- |
| PostgreSQL | TCP | 5432 | 내 IP | DataGrip 임시 접속 |

작업이 끝나면 이 규칙은 삭제합니다.

### 2.3 아웃바운드 규칙

보통 기본값 그대로 둡니다.

| 유형 | 프로토콜 | 포트 범위 | 대상 |
| --- | --- | --- | --- |
| 모든 트래픽 | 전체 | 전체 | `0.0.0.0/0` |

EC2가 GitHub, GHCR, Kakao API, 공공데이터 API, RDS로 나가야 하므로 초반에는 기본 아웃바운드 허용이 가장 단순합니다.

## 3. EC2 인스턴스 생성

Ubuntu 기준입니다.

권장 값:

| 항목 | 값 |
| --- | --- |
| AMI | Ubuntu Server 24.04 LTS 또는 22.04 LTS |
| Instance type | 테스트용 `t3.micro` 또는 `t3.small` |
| Key pair | 새로 생성하거나 기존 `.pem` 사용 |
| Network | RDS와 같은 VPC |
| Subnet | public subnet |
| Auto-assign public IP | Enable |
| Security group | 위에서 만든 EC2 보안 그룹 |
| Storage | 최소 20GB 권장 |

생성 후 접속:

```bash
chmod 400 /path/to/key.pem
ssh -i /path/to/key.pem ubuntu@<EC2_PUBLIC_IP>
```

## 4. EC2 초기 세팅

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin nginx git
sudo usermod -aG docker $USER
```

권한 적용을 위해 SSH를 다시 접속합니다.

```bash
docker version
docker compose version
```

## 5. 서버에 레포 준비

```bash
sudo mkdir -p /opt/pickitpickit
sudo chown -R $USER:$USER /opt/pickitpickit
git clone git@github.com:<OWNER>/<REPO>.git /opt/pickitpickit
cd /opt/pickitpickit
```

private repository라면 EC2에 GitHub deploy key를 등록해야 합니다.

### 5.1 private repository deploy key

EC2에서 SSH key를 만듭니다.

```bash
ssh-keygen -t ed25519 -C "pickitpickit-ec2-deploy" -f ~/.ssh/pickitpickit_deploy
cat ~/.ssh/pickitpickit_deploy.pub
```

GitHub:

```text
Repository -> Settings -> Deploy keys
-> Add deploy key
```

설정:

| 항목 | 값 |
| --- | --- |
| Title | `pickitpickit-ec2` |
| Key | EC2에서 출력한 public key |
| Allow write access | 체크하지 않음 |

EC2의 SSH 설정:

```bash
cat <<'EOF' >> ~/.ssh/config
Host github.com
  HostName github.com
  User git
  IdentityFile ~/.ssh/pickitpickit_deploy
  IdentitiesOnly yes
EOF

chmod 600 ~/.ssh/config
ssh -T git@github.com
```

정상이라면 GitHub 인증 메시지가 나옵니다.

## 6. 운영 환경변수 생성

EC2의 `/opt/pickitpickit/.env`에 운영 값을 저장합니다. 이 파일은 Git에 커밋하지 않습니다.

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/<DB_NAME>
SPRING_DATASOURCE_USERNAME=<DB_USERNAME>
SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_FLYWAY_ENABLED=true
SPRING_FLYWAY_LOCATIONS=classpath:db/migration

KAKAO_API_KEY=<KAKAO_REST_API_KEY>
KAKAO_API_BASE_URL=https://dapi.kakao.com
KAKAO_OAUTH_USER_INFO_URL=https://kapi.kakao.com/v2/user/me

PUBLIC_API_KEY=<PUBLIC_API_KEY>
PUBLIC_API_BASE_URL=https://apis.data.go.kr/1741000/youth_game_providers/info

CLAUDE_API_KEY=<ANTHROPIC_CLAUDE_API_KEY>
CLAUDE_API_BASE_URL=https://api.anthropic.com
CLAUDE_API_VERSION=2023-06-01
CLAUDE_API_MODEL=claude-sonnet-4-6

JWT_SECRET=<32_BYTES_OR_LONGER_SECRET>
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=1209600000
```

값 찾는 위치:

| 환경변수 | 어디서 확인 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | RDS -> 데이터베이스 -> 연결 & 보안 -> 엔드포인트 |
| `SPRING_DATASOURCE_USERNAME` | RDS 생성 시 입력한 마스터 사용자명 |
| `SPRING_DATASOURCE_PASSWORD` | RDS 생성 시 입력한 마스터 비밀번호 |
| `KAKAO_API_KEY` | Kakao Developers -> 내 애플리케이션 -> 앱 키 -> REST API 키 |
| `PUBLIC_API_KEY` | 공공데이터포털에서 발급받은 서비스 키 |
| `CLAUDE_API_KEY` | Anthropic Console -> API Keys에서 발급받은 키 |
| `JWT_SECRET` | 직접 생성한 32바이트 이상 문자열 |

`JWT_SECRET` 생성 예:

```bash
openssl rand -base64 48
```

DB URL 예:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://pickit-db.xxxxxx.ap-northeast-2.rds.amazonaws.com:5432/pickit
```

## 7. RDS 연결 확인

EC2에서 RDS로 접속 가능한지 확인합니다.

```bash
sudo apt install -y postgresql-client
psql "$SPRING_DATASOURCE_URL" -U "$SPRING_DATASOURCE_USERNAME"
```

환경변수를 shell에 export하지 않았다면 직접 입력합니다.

```bash
psql "host=<RDS_ENDPOINT> port=5432 dbname=<DB_NAME> user=<DB_USERNAME> password=<DB_PASSWORD> sslmode=require"
```

접속이 안 되면 먼저 확인합니다.

- EC2와 RDS가 같은 VPC인지
- RDS 보안 그룹 inbound `5432` 소스가 EC2 보안 그룹인지
- RDS가 `Available` 상태인지
- DB 이름, 사용자명, 비밀번호가 맞는지

## 8. Nginx 설정

`/etc/nginx/sites-available/pickitpickit`:

```nginx
server {
    listen 80;
    server_name pickit-pickit.site www.pickit-pickit.site;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

활성화:

```bash
sudo ln -s /etc/nginx/sites-available/pickitpickit /etc/nginx/sites-enabled/pickitpickit
sudo nginx -t
sudo systemctl reload nginx
```

HTTPS는 Certbot으로 연결합니다.

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d pickit-pickit.site -d www.pickit-pickit.site
```

## 9. 도메인 설정

도메인을 Route 53이 아니라 외부 도메인 업체에서 관리한다면, DNS 관리 화면에서 A 레코드를 추가합니다.

| Type | Name | Value |
| --- | --- | --- |
| A | `@` 또는 `pickit-pickit.site` | EC2 public IP |
| A | `www` | EC2 public IP |

Route 53을 쓴다면:

```text
Route 53 -> Hosted zones -> pickit-pickit.site
-> Create record
```

레코드:

| Record name | Record type | Value |
| --- | --- | --- |
| 비움 | A | EC2 public IP |
| `www` | A | EC2 public IP |

운영에서는 EC2 public IP가 바뀌지 않도록 Elastic IP 연결을 권장합니다.

```text
EC2 -> Elastic IPs
-> Allocate Elastic IP address
-> Associate Elastic IP address
-> 대상 EC2 인스턴스 선택
```

## 10. GitHub Packages 설정

이 workflow는 Docker 이미지를 GitHub Container Registry, 즉 GHCR에 올립니다.

이미지 이름 예:

```text
ghcr.io/<OWNER>/<REPO>:<COMMIT_SHA>
ghcr.io/<OWNER>/<REPO>:dev
```

private repository에서는 EC2가 GHCR 이미지를 pull할 수 있도록 GitHub PAT가 필요합니다.

GitHub:

```text
프로필 사진 -> Settings
-> Developer settings
-> Personal access tokens
-> Tokens (classic)
-> Generate new token
```

권장 권한:

| 권한 | 용도 |
| --- | --- |
| `read:packages` | EC2에서 GHCR 이미지 pull |

private repo/package 접근이 막히면 `repo` 권한이 추가로 필요할 수 있습니다.

## 11. GitHub Secrets

Repository Settings -> Secrets and variables -> Actions에 추가합니다.

```text
EC2_HOST       EC2 public IP 또는 도메인
EC2_PORT       22
EC2_USER       ubuntu
EC2_SSH_KEY    EC2 접속 private key 전체 내용
EC2_APP_DIR    /opt/pickitpickit
GHCR_USERNAME  GitHub username
GHCR_TOKEN     GHCR read 권한이 있는 GitHub PAT
```

`GHCR_TOKEN`은 private package pull을 위해 필요합니다. 권한은 최소 `read:packages`를 사용합니다.

각 값:

| Secret | 예시 | 설명 |
| --- | --- | --- |
| `EC2_HOST` | `13.125.xxx.xxx` | EC2 public IP 또는 도메인 |
| `EC2_PORT` | `22` | SSH 포트 |
| `EC2_USER` | `ubuntu` | Ubuntu AMI 기본 사용자 |
| `EC2_SSH_KEY` | `-----BEGIN OPENSSH PRIVATE KEY-----...` | EC2 접속용 private key 전체 내용 |
| `EC2_APP_DIR` | `/opt/pickitpickit` | EC2의 레포 경로 |
| `GHCR_USERNAME` | GitHub username | GHCR 로그인 사용자 |
| `GHCR_TOKEN` | `github_pat_...` | `read:packages` 권한 PAT |

`EC2_SSH_KEY`에는 `.pem` 파일 내용을 그대로 넣습니다.

```bash
cat /path/to/key.pem
```

출력된 전체 내용을 Secret 값으로 저장합니다.

## 12. 최초 수동 실행

GitHub Actions를 돌리기 전에 EC2에서 한 번 수동으로 컨테이너가 뜨는지 확인하면 문제를 분리하기 쉽습니다.

```bash
cd /opt/pickitpickit
export APP_IMAGE=ghcr.io/<OWNER>/<REPO>:dev
docker login ghcr.io -u <GITHUB_USERNAME>
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
docker compose -f docker-compose.prod.yml logs -f --tail=200
curl -f http://127.0.0.1:8080/actuator/health
```

아직 이미지가 없다면 GitHub Actions를 먼저 한 번 실행해서 이미지를 만들어야 합니다.

## 13. 배포

`dev`에 push하면 `.github/workflows/deploy-ec2.yml`이 실행됩니다.

수동 실행도 가능합니다.

```text
GitHub Actions -> Deploy to EC2 -> Run workflow
```

## 14. 확인 명령

EC2:

```bash
cd /opt/pickitpickit
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f --tail=200
curl -f http://127.0.0.1:8080/actuator/health
```

외부:

```bash
curl -I https://pickit-pickit.site
curl https://pickit-pickit.site/actuator/health
curl https://pickit-pickit.site/swagger-ui/index.html
```

장애가 나면 먼저 앱 로그와 RDS 연결 정보를 확인합니다.

```bash
docker compose -f docker-compose.prod.yml logs --tail=200 api
```

자주 보는 오류:

| 증상 | 우선 확인 |
| --- | --- |
| `Connection refused` | RDS 보안 그룹, DB endpoint, DB 포트 |
| `password authentication failed` | DB username/password |
| `database does not exist` | DB 이름 |
| `JWT_SECRET은 32바이트 이상` | `.env`의 `JWT_SECRET` 길이 |
| `Could not resolve placeholder KAKAO_API_KEY` | `.env` 누락 또는 compose env_file 경로 |
| AI 추천이 fallback 사유만 반환 | `.env`의 `CLAUDE_API_KEY`, Anthropic API 키 권한 |
| Nginx `502 Bad Gateway` | Docker 컨테이너 실행 여부, `127.0.0.1:8080` 포트 |
