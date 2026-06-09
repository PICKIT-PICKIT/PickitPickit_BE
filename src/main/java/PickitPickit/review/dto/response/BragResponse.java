package PickitPickit.review.dto.response;

import PickitPickit.review.domain.Brag;
import PickitPickit.user.domain.User;

import java.time.LocalDateTime;

public record BragResponse(
        Long bragId,
        Long userId,
        String authorNickname,
        String authorProfileImageUrl,
        Long storeId,
        int spentCost,
        String imageUrl,
        String content,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static BragResponse from(Brag brag) {
        return from(brag, null);
    }

    public static BragResponse from(Brag brag, User author) {
        boolean withdrawn = author == null || author.isWithdrawn();

        return new BragResponse(
                brag.getId(),
                withdrawn ? null : brag.getUserId(),
                withdrawn ? "탈퇴한 사용자" : author.getDisplayNickname(),
                withdrawn ? null : author.getDisplayProfileImageUrl(),
                brag.getStoreId(),
                brag.getSpentCost(),
                brag.getImageUrl(),
                brag.getContent(),
                brag.getCreatedAt(),
                brag.getModifiedAt()
        );
    }
}