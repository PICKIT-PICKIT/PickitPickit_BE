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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final BragService bragService;

    @GetMapping("/write-guide")
    public ResponseEntity<ApiResponse<ReviewWriteGuideResponse>> getWriteGuide() {
        return ApiResponse.success(SuccessStatus.FETCHED, reviewService.getWriteGuide());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, reviewService.createReview(request));
    }

    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<StoreReviewSummaryResponse>> getStoreReviews(
            @PathVariable Long storeId
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, reviewService.getStoreReviews(storeId));
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, reviewService.updateReview(reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }

    @PostMapping("/brags")
    public ResponseEntity<ApiResponse<BragResponse>> createBrag(
            @Valid @RequestBody BragCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, bragService.createBrag(request));
    }

    @GetMapping("/brags")
    public ResponseEntity<ApiResponse<List<BragResponse>>> getBrags() {
        return ApiResponse.success(SuccessStatus.FETCHED, bragService.getBrags());
    }

    @PatchMapping("/brags/{bragId}")
    public ResponseEntity<ApiResponse<BragResponse>> updateBrag(
            @PathVariable Long bragId,
            @Valid @RequestBody BragUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, bragService.updateBrag(bragId, request));
    }

    @DeleteMapping("/brags/{bragId}")
    public ResponseEntity<ApiResponse<Void>> deleteBrag(
            @PathVariable Long bragId,
            @RequestParam Long userId
    ) {
        bragService.deleteBrag(bragId, userId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }
}