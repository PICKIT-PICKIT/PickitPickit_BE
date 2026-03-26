package PickitPickit.review.dto.response;

import PickitPickit.review.domain.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        Long userId,
        Long storeId,
        double rating,
        Integer difficulty,
        String difficultyLabel,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getStoreId(),
                review.getRating(),
                review.getDifficulty(),
                toDifficultyLabel(review.getDifficulty()),
                review.getContent(),
                review.getImageUrl(),
                review.getCreatedAt(),
                review.getModifiedAt()
        );
    }

    private static String toDifficultyLabel(Integer difficulty) {
        if (difficulty == null) return null;

        return switch (difficulty) {
            case 1 -> "매우 쉬움";
            case 2 -> "쉬움";
            case 3 -> "보통";
            case 4 -> "어려움";
            case 5 -> "매우 어려움";
            default -> null;
        };
    }
}