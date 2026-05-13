package PickitPickit.auth.controller;

import PickitPickit.auth.dto.request.KakaoLoginRequest;
import PickitPickit.auth.dto.response.LoginResponse;
import PickitPickit.auth.service.AuthService;
import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
