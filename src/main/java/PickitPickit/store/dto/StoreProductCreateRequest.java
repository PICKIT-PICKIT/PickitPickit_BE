package PickitPickit.store.dto;

import PickitPickit.store.domain.InventoryMode;
import PickitPickit.store.domain.StockStatus;
import jakarta.validation.constraints.*;

import java.util.List;

public record StoreProductCreateRequest(
        @NotNull(message = "storeId는 필수입니다.")
        Long storeId,

        @NotNull(message = "itemId는 필수입니다.")
        Long itemId,

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

        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
