package PickitPickit.search.dto.request;

import PickitPickit.search.domain.SearchTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SearchLogCreateRequest(

        Long userId,

        @NotBlank(message = "keyword는 필수입니다.")
        @Size(max = 255, message = "keyword는 255자 이하여야 합니다.")
        String keyword,

        @NotNull(message = "targetType은 필수입니다.")
        SearchTargetType targetType
) {
}
