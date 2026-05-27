package PickitPickit.store.service;

import PickitPickit.store.dto.FavoriteStoreResponse;

import java.util.List;

public interface FavoriteStoreService {

    FavoriteStoreResponse addFavoriteStore(Long userId, Long storeId);

    void removeFavoriteStore(Long userId, Long storeId);

    List<FavoriteStoreResponse> getMyFavoriteStores(Long userId, Double userLat, Double userLng);
}