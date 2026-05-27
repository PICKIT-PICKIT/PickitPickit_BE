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
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        User user = getActiveUser(request.userId());

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

        return ReviewResponse.from(reviewRepository.save(review), user);
    }

    @Override
    public StoreReviewSummaryResponse getStoreReviews(Long storeId) {
        List<Review> reviewEntities = reviewRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
        Map<Long, User> authorMap = getAuthorMap(reviewEntities.stream()
                .map(Review::getUserId)
                .toList());

        List<ReviewResponse> reviews = reviewEntities.stream()
                .map(review -> ReviewResponse.from(review, authorMap.get(review.getUserId())))
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
    public List<ReviewResponse> getMyReviews(Long userId) {
        User user = getActiveUser(userId);

        return reviewRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(review -> ReviewResponse.from(review, user))
                .toList();
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewUpdateRequest request) {
        User user = getActiveUser(request.userId());

        Review review = reviewRepository.findByIdAndUserId(reviewId, request.userId())
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "수정할 후기를 찾을 수 없습니다."));

        review.update(
                request.rating(),
                request.difficulty(),
                request.content(),
                request.imageUrl()
        );

        return ReviewResponse.from(review, user);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        getActiveUser(userId);

        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "삭제할 후기를 찾을 수 없습니다."));

        reviewRepository.delete(review);
    }

    @Override
    public ReviewWriteGuideResponse getWriteGuide() {
        return ReviewWriteGuideResponse.defaultGuide();
    }

    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
    }

    private Map<Long, User> getAuthorMap(List<Long> userIds) {
        return userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }
}