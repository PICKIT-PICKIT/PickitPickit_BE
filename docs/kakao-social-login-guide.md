# 카카오 소셜 로그인 연동 가이드

## Android 로그인 흐름

1. Android에서 Kakao SDK 로그인을 호출합니다.
   - 기본적으로 `loginWithKakaoTalk()`을 우선 사용합니다.
   - 카카오톡 로그인이 불가능하거나 적절하지 않은 경우 `loginWithKakaoAccount()`로 fallback합니다.
2. Android는 Kakao SDK가 발급한 `OAuthToken.accessToken`만 백엔드로 전달합니다.
   - `POST /api/auth/kakao/login`
   - 요청 body: `{ "kakaoAccessToken": "..." }`
3. 백엔드는 전달받은 카카오 토큰을 검증하기 위해 카카오 사용자 정보 API를 내부에서 호출합니다.
   - `GET https://kapi.kakao.com/v2/user/me`
   - 이 API는 카카오 외부 API이므로 우리 서비스 Swagger API 목록에는 나타나지 않습니다.
4. 백엔드는 `kakao_id` 기준으로 사용자를 조회하거나 생성한 뒤, 우리 서비스 토큰을 반환합니다.
   - `data.accessToken`: 보호된 백엔드 API 호출 시 `Authorization: Bearer {accessToken}`으로만 사용합니다.
   - `data.refreshToken`: 토큰 재발급과 서비스 로그아웃에만 사용합니다.
   - `data.user`: 초기 프로필 표시와 앱의 로그인 상태 구성에 사용합니다.

## 전체 인증 흐름

### 1. 로그인

```text
Android
Kakao SDK 로그인 실행
    ↓
Kakao
카카오 access token 발급
    ↓
Android
POST /api/auth/kakao/login
body: { "kakaoAccessToken": "{카카오 access token}" }
    ↓
Backend
카카오 /v2/user/me 호출
카카오 회원번호(kakao_id) 기준으로 users 조회 또는 생성
서비스 accessToken + refreshToken 발급
refreshToken은 SHA-256 해시로 DB 저장
    ↓
Android
서비스 accessToken, refreshToken, user 저장
```

로그인 API에 전달하는 토큰은 **카카오 access token**입니다. 로그인 API 응답으로 받는 `accessToken`, `refreshToken`은 **우리 서비스 JWT**입니다. 두 토큰은 서로 다른 토큰이므로 혼동하지 않습니다.

### 2. 일반 API 호출

```text
Android
Authorization: Bearer {서비스 accessToken}
    ↓
Backend
jwtDecoder가 JWT 검증
- 우리 서버가 서명한 토큰인지 확인
- 만료 여부 확인
- issuer가 pickitpickit인지 확인
- token_type이 access인지 확인
    ↓
Backend
JWT subject(sub)에서 userId 추출
```

일반 API 접근에는 서비스 `accessToken`만 사용합니다. `refreshToken`은 일반 API 접근에 사용할 수 없습니다.

예시:

```http
GET /api/auth/me
Authorization: Bearer {accessToken}
```

### 3. 토큰 재발급

```text
Android
POST /api/auth/token/reissue
body: { "refreshToken": "{서비스 refreshToken}" }
    ↓
Backend
refreshTokenJwtDecoder가 refreshToken 검증
- 우리 서버가 서명한 토큰인지 확인
- 만료 여부 확인
- issuer가 pickitpickit인지 확인
- token_type이 refresh인지 확인
    ↓
Backend
요청받은 refreshToken을 SHA-256 해시로 변환
refresh_tokens.token_hash와 비교
    ↓
Backend
DB에 저장된 현재 활성 refreshToken이면 새 accessToken + 새 refreshToken 발급
기존 refreshToken 해시는 삭제하고 새 refreshToken 해시로 교체
    ↓
Android
기존 accessToken, refreshToken을 버리고 새 토큰으로 교체
```

재발급에 성공하면 기존 refreshToken은 즉시 폐기됩니다. 같은 refreshToken으로 재발급 API를 두 번 호출하면 두 번째 요청은 실패하는 것이 정상입니다.

### 4. 로그아웃

```text
Android
POST /api/auth/logout
body: { "refreshToken": "{서비스 refreshToken}" }
    ↓
Backend
refreshToken을 SHA-256 해시로 변환
DB에서 해당 token_hash 삭제
    ↓
Android
로컬에 저장된 서비스 accessToken, refreshToken 삭제
Kakao SDK logout() 호출
로그인 화면으로 이동
```

서비스 로그아웃은 우리 서버의 refreshToken을 폐기하는 작업입니다. Kakao SDK `logout()`은 Android에 저장된 카카오 토큰을 폐기하는 작업입니다. 두 작업은 역할이 다르므로 Android에서 함께 처리합니다.

## 토큰 생명주기

- access token은 stateless JWT이며 DB에 저장하지 않습니다.
- refresh token은 재발급 시마다 rotate되며, `refresh_tokens` 테이블에 SHA-256 해시로만 저장합니다.
- v1에서는 사용자당 활성 refresh token 1개만 지원합니다.
- `POST /api/auth/token/reissue`는 새로운 access token과 새로운 refresh token을 반환합니다.
- Android는 재발급 성공 후 이전 refresh token을 반드시 폐기하고 새 refresh token으로 교체해야 합니다.

## 로그아웃 흐름

1. Android는 현재 서비스 refresh token으로 `POST /api/auth/logout`을 호출합니다.
2. 백엔드는 저장된 refresh token 해시를 삭제합니다.
3. Android는 API 성공 여부와 관계없이 로컬에 저장된 서비스 토큰을 삭제합니다.
4. Android는 Kakao SDK `logout()`을 호출해 Kakao SDK에 저장된 카카오 토큰도 폐기합니다.

로그아웃은 회원탈퇴가 아닙니다. 카카오 연결 해제와 서비스 회원탈퇴는 추후 별도 흐름으로 구현해야 합니다.

## Android 저장소 권장사항

- 서비스 토큰을 일반 SharedPreferences에 평문으로 저장하지 않습니다.
- `EncryptedSharedPreferences` 또는 프로젝트에서 승인한 보안 저장소 래퍼를 사용합니다.
- Android 백업 기능을 사용하는 경우 토큰 저장소는 백업/복원 대상에서 제외합니다.
