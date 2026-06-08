package PickitPickit.store.dto;

import PickitPickit.store.domain.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ItemUpdateRequest(
        @Schema(description = "상품 마스터 이름", example = "손오공 피규어")
        @NotBlank(message = "상품명은 필수입니다.")
        @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
        String name,

        @Schema(description = "상품 카테고리. 사용 가능 값: PLUSH, FIGURE, KEYRING, GACHA, SNACK, ETC", example = "PLUSH")
        @NotNull(message = "상품 카테고리는 필수입니다.")
        ProductCategory category,

        @Schema(description = "상품 마스터 기본 이미지 URL. 없으면 null로 보냅니다.", example = "https://example.com/items/songoku.png")
        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String defaultImageUrl,

        @Schema(description = "상품 마스터 기본 태그명 목록. 없는 태그는 자동 생성되고 중복 태그는 normalize 후 제거됩니다.", example = "[\"드래곤볼\", \"손오공\"]")
        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
