package PickitPickit.store.service;

import PickitPickit.store.dto.StoreRecommendationResponse;
import PickitPickit.store.dto.StoreType;

import java.util.List;

public interface StoreRecommendationService {

    List<StoreRecommendationResponse> recommendStores(
            Long userId,
            String keyword,
            double latitude,
            double longitude,
            StoreType type,
            int limit
    );
}
