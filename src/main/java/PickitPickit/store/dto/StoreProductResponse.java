package PickitPickit.store.dto;

import PickitPickit.store.domain.StoreProduct;

import java.util.List;

public record StoreProductResponse(
        Long productId,
        Long storeId,
        Long itemId,
        String itemName,
        String category,
        Integer price,
        String inventoryMode,
        Integer stockQuantity,
        String stockStatus,
        Integer difficulty,
        String difficultyLabel,
        String imageUrl,
        List<TagResponse> tags
) {
    public static StoreProductResponse from(StoreProduct sp, List<TagResponse> tags) {
        return new StoreProductResponse(
                sp.getId(),
                sp.getStore().getId(),
                sp.getItem().getId(),
                sp.getItem().getName(),
                sp.getItem().getCategory().name(),
                sp.getPrice(),
                sp.getInventoryMode().name(),
                sp.getStockQuantity(),
                sp.getStockStatus().name(),
                sp.getDifficulty(),
                sp.getDifficultyLabel(),
                sp.getEffectiveImageUrl(),
                tags
        );
    }
}
