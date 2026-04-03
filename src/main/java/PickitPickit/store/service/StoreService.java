package PickitPickit.store.service;

import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;

import java.util.List;

public interface StoreService {

    /**
     * 현재 위치 기반 주변 매장 조회 (DB 기반, 거리순 정렬)
     *
     * @param latitude      사용자 위도
     * @param longitude     사용자 경도
     * @param radiusMeters  검색 반경 (500 / 1000 / 3000 / 5000 m)
     * @param type          매장 유형 (CLAW / GACHA / ALL)
     * @return 거리순 정렬된 주변 매장 목록
     */
    List<StoreResponse> getNearbyStores(double latitude, double longitude,
                                        int radiusMeters, StoreType type);

    /**
     * 매장 상세 조회 (재고 포함)
     *
     * @param storeId 매장 DB ID
     * @param userLat 사용자 위도 (거리 계산용, 선택)
     * @param userLng 사용자 경도 (거리 계산용, 선택)
     * @return 매장 정보 + 등록된 상품/재고 목록
     */
    StoreDetailResponse getStoreDetail(Long storeId, Double userLat, Double userLng);
}
