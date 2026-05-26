package PickitPickit.store.dto;

import PickitPickit.store.domain.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ItemCreateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "상품 카테고리는 필수입니다.")
        ProductCategory category,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String defaultImageUrl,

        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
