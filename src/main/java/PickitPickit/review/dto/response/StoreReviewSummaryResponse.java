package PickitPickit.review.dto.response;

import java.util.List;

public record StoreReviewSummaryResponse(
        Long storeId,
        long reviewCount,
        Double averageRating,
        boolean showRating,
        Double averageDifficulty,
        boolean showDifficulty,
        List<ReviewResponse> reviews
) {
}