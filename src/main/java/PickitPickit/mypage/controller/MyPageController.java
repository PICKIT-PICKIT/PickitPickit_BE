package PickitPickit.mypage.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.mypage.dto.request.MyPageProfileUpdateRequest;
import PickitPickit.mypage.dto.response.MyPageProfileResponse;
import PickitPickit.mypage.service.MyPageService;
import PickitPickit.review.dto.response.BragResponse;
import PickitPickit.review.dto.response.ReviewResponse;
import PickitPickit.review.service.BragService;
import PickitPickit.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MyPage", description = "마이페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
@SecurityRequirement(name = "bearerAuth")
public class MyPageController {

    private final MyPageService myPageService;
    private final ReviewService reviewService;
    private final BragService bragService;

    @Operation(
            summary = "마이페이지 프로필 조회",
            description = "현재 로그인 사용자의 프로필 정보, 관심 태그, 기본 이미지 후보, 활동 개수를 조회합니다."
    )
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<MyPageProfileResponse>> getProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.FETCHED,
                myPageService.getProfile(getUserId(jwt))
        );
    }

    @Operation(
            summary = "마이페이지 프로필 수정",
            description = "닉네임, 프로필 이미지, 관심 태그를 수정합니다. 앨범 업로드는 지원하지 않습니다."
    )
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<MyPageProfileResponse>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody MyPageProfileUpdateRequest request
    ) {
        return ApiResponse.success(
                SuccessStatus.UPDATED,
                myPageService.updateProfile(getUserId(jwt), request)
        );
    }

    @Operation(
            summary = "내가 작성한 리뷰 목록 조회",
            description = "현재 로그인 사용자가 작성한 리뷰 목록을 최신순으로 조회합니다."
    )
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.FETCHED,
                reviewService.getMyReviews(getUserId(jwt))
        );
    }

    @Operation(
            summary = "내가 작성한 자랑하기 목록 조회",
            description = "현재 로그인 사용자가 작성한 자랑하기 게시글 목록을 최신순으로 조회합니다."
    )
    @GetMapping("/brags")
    public ResponseEntity<ApiResponse<List<BragResponse>>> getMyBrags(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                SuccessStatus.FETCHED,
                bragService.getMyBrags(getUserId(jwt))
        );
    }

    private Long getUserId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}