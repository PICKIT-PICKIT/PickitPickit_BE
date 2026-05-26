package PickitPickit.store.dto;

import PickitPickit.store.domain.InventoryMode;
import PickitPickit.store.domain.StockStatus;
import jakarta.validation.constraints.*;

import java.util.List;

public record StoreProductUpdateRequest(
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "inventoryMode는 필수입니다.")
        InventoryMode inventoryMode,

        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        StockStatus stockStatus,

        @Min(value = 1, message = "상품 난이도는 1 이상이어야 합니다.")
        @Max(value = 5, message = "상품 난이도는 5 이하여야 합니다.")
        Integer difficulty,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl,

        /** null이면 기존 태그 유지, 빈 배열이면 전체 삭제 */
        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
