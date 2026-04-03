package PickitPickit.review.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.review.dto.request.BragCreateRequest;
import PickitPickit.review.dto.request.BragUpdateRequest;
import PickitPickit.review.dto.request.ReviewCreateRequest;
import PickitPickit.review.dto.request.ReviewUpdateRequest;
import PickitPickit.review.dto.response.BragResponse;
import PickitPickit.review.dto.response.ReviewResponse;
import PickitPickit.review.dto.response.ReviewWriteGuideResponse;
import PickitPickit.review.dto.response.StoreReviewSummaryResponse;
import PickitPickit.review.service.BragService;
import PickitPickit.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Review & Brag", description = "리뷰 및 자랑하기 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final BragService bragService;

    @Operation(summary = "리뷰 작성 가이드 조회", description = "리뷰 작성 시 안내 문구를 반환합니다.")
    @GetMapping("/write-guide")
    public ResponseEntity<ApiResponse<ReviewWriteGuideResponse>> getWriteGuide() {
        return ApiResponse.success(SuccessStatus.FETCHED, reviewService.getWriteGuide());
    }

    @Operation(summary = "리뷰 작성", description = "특정 매장에 대한 리뷰를 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, reviewService.createReview(request));
    }

    @Operation(summary = "매장 리뷰 목록 조회", description = "특정 매장의 리뷰 목록 및 요약 정보를 조회합니다.")
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreReviewSummaryResponse>> getStoreReviews(
            @Parameter(description = "매장 ID (DB PK)", required = true)
            @PathVariable Long storeId
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, reviewService.getStoreReviews(storeId));
    }

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다.")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, reviewService.updateReview(reviewId, request));
    }

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }

    // ─── Brag ──────────────────────────────────────────────────────────────────

    @Operation(summary = "자랑하기 게시글 작성", description = "인형뽑기 결과물 자랑 게시글을 작성합니다.")
    @PostMapping("/brags")
    public ResponseEntity<ApiResponse<BragResponse>> createBrag(
            @Valid @RequestBody BragCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, bragService.createBrag(request));
    }

    @Operation(summary = "자랑하기 게시글 목록 조회", description = "전체 자랑하기 게시글 목록을 조회합니다.")
    @GetMapping("/brags")
    public ResponseEntity<ApiResponse<List<BragResponse>>> getBrags() {
        return ApiResponse.success(SuccessStatus.FETCHED, bragService.getBrags());
    }

    @Operation(summary = "자랑하기 게시글 수정", description = "자랑하기 게시글을 수정합니다.")
    @PatchMapping("/brags/{bragId}")
    public ResponseEntity<ApiResponse<BragResponse>> updateBrag(
            @Parameter(description = "자랑하기 게시글 ID", required = true)
            @PathVariable Long bragId,
            @Valid @RequestBody BragUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, bragService.updateBrag(bragId, request));
    }

    @Operation(summary = "자랑하기 게시글 삭제", description = "자랑하기 게시글을 삭제합니다.")
    @DeleteMapping("/brags/{bragId}")
    public ResponseEntity<ApiResponse<Void>> deleteBrag(
            @Parameter(description = "자랑하기 게시글 ID", required = true)
            @PathVariable Long bragId,
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId
    ) {
        bragService.deleteBrag(bragId, userId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }
}