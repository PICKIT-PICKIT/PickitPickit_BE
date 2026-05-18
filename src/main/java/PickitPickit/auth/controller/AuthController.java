package PickitPickit.auth.controller;

import PickitPickit.auth.dto.request.KakaoLoginRequest;
import PickitPickit.auth.dto.request.LogoutRequest;
import PickitPickit.auth.dto.request.RefreshTokenRequest;
import PickitPickit.auth.dto.response.AuthUserResponse;
import PickitPickit.auth.dto.response.LoginResponse;
import PickitPickit.auth.dto.response.TokenResponse;
import PickitPickit.auth.service.AuthService;
import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "카카오 로그인",
            description = "Android Kakao SDK에서 발급받은 OAuthToken.accessToken을 전달하면 서비스 JWT를 발급합니다."
    )
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithKakao(
            @Valid @RequestBody KakaoLoginRequest request
    ) {
        return ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, authService.loginWithKakao(request));
    }

    @Operation(
            summary = "토큰 재발급",
            description = "서비스 refresh token을 검증하고 새 access token과 refresh token을 발급합니다."
    )
    @PostMapping("/token/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, authService.reissue(request));
    }

    @Operation(
            summary = "내 정보 조회",
            description = "서비스 access token으로 현재 로그인한 사용자의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getMe(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, authService.getCurrentUser(Long.parseLong(jwt.getSubject())));
    }

    @Operation(
            summary = "로그아웃",
            description = "서비스 refresh token을 폐기합니다. Android에서는 이 API 성공 후 Kakao SDK logout도 함께 호출하세요."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);
        return ApiResponse.success(SuccessStatus.LOGOUT_SUCCESS);
    }
}
