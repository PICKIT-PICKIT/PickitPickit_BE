package PickitPickit.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BragUpdateRequest(

        @NotNull(message = "userId는 필수입니다.")
        Long userId,

        Long storeId,

        @NotNull(message = "spentCost는 필수입니다.")
        @PositiveOrZero(message = "사용 금액은 0 이상이어야 합니다.")
        Integer spentCost,

        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl,

        @Size(max = 1000, message = "자랑 멘트는 1000자 이하여야 합니다.")
        String content
) {
}