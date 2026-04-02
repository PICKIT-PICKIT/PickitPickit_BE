package PickitPickit.store.dto;

import PickitPickit.store.domain.StoreProduct;

import java.util.List;

/**
 * 매장 상세 조회 응답 (재고 포함)
 */
public record StoreDetailResponse(
        StoreResponse store,
        List<ProductInfo> products
) {

    public record ProductInfo(
            Long productId,
            Long itemId,
            String itemName,
            String category,
            Integer price,
            String inventoryMode,
            Integer stockQuantity,
            String stockStatus,
            String imageUrl
    ) {
        public static ProductInfo from(StoreProduct sp) {
            return new ProductInfo(
                    sp.getId(),
                    sp.getItem().getId(),
                    sp.getItem().getName(),
                    sp.getItem().getCategory().name(),
                    sp.getPrice(),
                    sp.getInventoryMode().name(),
                    sp.getStockQuantity(),
                    sp.getStockStatus() != null ? sp.getStockStatus().name() : null,
                    sp.getEffectiveImageUrl()
            );
        }
    }
}
