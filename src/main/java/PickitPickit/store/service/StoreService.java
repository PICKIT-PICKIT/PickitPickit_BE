package PickitPickit.store.service;

import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;

import java.util.List;

public interface StoreService {

    List<StoreResponse> getNearbyStores(double latitude, double longitude, int radiusMeters, StoreType type);

    StoreDetailResponse getStoreDetail(Long storeId, Double userLat, Double userLng);

    List<StoreResponse> searchStores(String keyword, StoreType type, Double userLat, Double userLng, int limit);
}
