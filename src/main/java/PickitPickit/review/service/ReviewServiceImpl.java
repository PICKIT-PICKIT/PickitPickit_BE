package PickitPickit.review.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.review.domain.Review;
import PickitPickit.review.dto.request.ReviewCreateRequest;
import PickitPickit.review.dto.request.ReviewUpdateRequest;
import PickitPickit.review.dto.response.ReviewResponse;
import PickitPickit.review.dto.response.ReviewWriteGuideResponse;
import PickitPickit.review.dto.response.StoreReviewSummaryResponse;
import PickitPickit.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        if (reviewRepository.existsByUserIdAndStoreId(request.userId(), request.storeId())) {
            throw new ApiException(ErrorStatus.DUPLICATE_RESOURCE, "한 매장에는 후기 1개만 작성할 수 있습니다.");
        }

        Review review = Review.create(
                request.userId(),
                request.storeId(),
                request.rating(),
                request.difficulty(),
                request.content(),
                request.imageUrl()
        );

        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Override
    public StoreReviewSummaryResponse getStoreReviews(Long storeId) {
        List<ReviewResponse> reviews = reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId)
                .stream()
                .map(ReviewResponse::from)
                .toList();

        long reviewCount = reviewRepository.countByStoreId(storeId);

        Double averageRating = reviewRepository.findAverageRatingByStoreId(storeId);
        if (averageRating != null) {
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }

        Double averageDifficulty = reviewRepository.findAverageDifficultyByStoreId(storeId);
        if (averageDifficulty != null) {
            averageDifficulty = Math.round(averageDifficulty * 10.0) / 10.0;
        }

        boolean showRating = reviewCount > 0;
        boolean showDifficulty = averageDifficulty != null;

        return new StoreReviewSummaryResponse(
                storeId,
                reviewCount,
                showRating ? averageRating : null,
                showRating,
                averageDifficulty,
                showDifficulty,
                reviews
        );
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, request.userId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "수정할 후기를 찾을 수 없습니다."));

        review.update(
                request.rating(),
                request.difficulty(),
                request.content(),
                request.imageUrl()
        );

        return ReviewResponse.from(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "삭제할 후기를 찾을 수 없습니다."));

        reviewRepository.delete(review);
    }

    @Override
    public ReviewWriteGuideResponse getWriteGuide() {
        return ReviewWriteGuideResponse.defaultGuide();
    }
}