package PickitPickit.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequest(

        @NotNull(message = "userId는 필수입니다.")
        Long userId,

        @NotNull(message = "rating은 필수입니다.")
        Double rating,

        @Min(value = 1, message = "난이도는 1 이상이어야 합니다.")
        @Max(value = 5, message = "난이도는 5 이하여야 합니다.")
        Integer difficulty,

        @Size(max = 1000, message = "후기 멘트는 1000자 이하여야 합니다.")
        String content,

        String imageUrl
) {
}