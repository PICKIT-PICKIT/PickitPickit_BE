package PickitPickit.review.service;

import PickitPickit.review.dto.request.ReviewCreateRequest;
import PickitPickit.review.dto.request.ReviewUpdateRequest;
import PickitPickit.review.dto.response.ReviewResponse;
import PickitPickit.review.dto.response.ReviewWriteGuideResponse;
import PickitPickit.review.dto.response.StoreReviewSummaryResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(ReviewCreateRequest request);

    StoreReviewSummaryResponse getStoreReviews(Long storeId);

    List<ReviewResponse> getMyReviews(Long userId);

    ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request);

    void deleteReview(Long reviewId, Long userId);

    ReviewWriteGuideResponse getWriteGuide();
}