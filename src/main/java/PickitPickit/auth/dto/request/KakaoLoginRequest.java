package PickitPickit.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(

        @Schema(description = "Android Kakao SDK OAuthToken.accessToken", example = "kakao-access-token")
        @NotBlank(message = "카카오 액세스 토큰은 필수입니다.")
        String kakaoAccessToken
) {
}
