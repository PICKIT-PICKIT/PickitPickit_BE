package PickitPickit.onboarding.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.onboarding.dto.request.InterestTagUpdateRequest;
import PickitPickit.onboarding.dto.request.NicknameUpdateRequest;
import PickitPickit.onboarding.dto.request.ProfileImageUpdateRequest;
import PickitPickit.onboarding.dto.response.*;
import PickitPickit.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Onboarding", description = "초기 프로필 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
@SecurityRequirement(name = "bearerAuth")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Operation(summary = "온보딩 상태 조회", description = "현재 로그인 사용자의 온보딩 상태를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getStatus(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, onboardingService.getStatus(getUserId(jwt)));
    }

    @Operation(summary = "닉네임 저장", description = "프론트에서 입력 또는 랜덤 생성한 닉네임을 저장합니다.")
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> updateNickname(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody NicknameUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, onboardingService.updateNickname(getUserId(jwt), request));
    }

    @Operation(summary = "프로필 이미지 후보 조회", description = "카카오 프로필 이미지 URL과 허용된 기본 이미지 URL을 조회합니다.")
    @GetMapping("/profile-images")
    public ResponseEntity<ApiResponse<ProfileImageOptionsResponse>> getProfileImages(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, onboardingService.getProfileImages(getUserId(jwt)));
    }

    @Operation(summary = "프로필 이미지 저장", description = "카카오 또는 기본 프로필 이미지 URL을 저장합니다.")
    @PatchMapping("/profile-image")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> updateProfileImage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfileImageUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, onboardingService.updateProfileImage(getUserId(jwt), request));
    }

    @Operation(summary = "관심 태그 목록 조회", description = "선택 가능한 관심 태그 목록을 조회합니다.")
    @GetMapping("/interest-tags")
    public ResponseEntity<ApiResponse<List<InterestTagResponse>>> getInterestTags() {
        return ApiResponse.success(SuccessStatus.FETCHED, onboardingService.getInterestTags());
    }

    @Operation(summary = "관심 태그 저장", description = "사용자가 선택한 관심 태그를 저장합니다.")
    @PatchMapping("/interest-tags")
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> updateInterestTags(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody InterestTagUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, onboardingService.updateInterestTags(getUserId(jwt), request));
    }

    @Operation(summary = "온보딩 완료", description = "필수 온보딩 정보가 모두 설정되었는지 검증하고 완료 처리합니다.")
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<OnboardingCompleteResponse>> complete(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, onboardingService.complete(getUserId(jwt)));
    }

    private Long getUserId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
