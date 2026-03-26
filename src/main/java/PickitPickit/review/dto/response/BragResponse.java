package PickitPickit.review.dto.response;

import PickitPickit.review.domain.Brag;

import java.time.LocalDateTime;

public record BragResponse(
        Long bragId,
        Long userId,
        Long storeId,
        int spentCost,
        String imageUrl,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static BragResponse from(Brag brag) {
        return new BragResponse(
                brag.getId(),
                brag.getUserId(),
                brag.getStoreId(),
                brag.getSpentCost(),
                brag.getImageUrl(),
                brag.getContent(),
                brag.getCreatedAt(),
                brag.getModifiedAt()
        );
    }
}