package PickitPickit.store.dto;

import PickitPickit.store.domain.FavoriteStore;

public record FavoriteStoreResponse(
        Long favoriteStoreId,
        StoreResponse store
) {
    public static FavoriteStoreResponse from(FavoriteStore favoriteStore, int distanceMeters) {
        return new FavoriteStoreResponse(
                favoriteStore.getId(),
                StoreResponse.from(favoriteStore.getStore(), distanceMeters)
        );
    }
}