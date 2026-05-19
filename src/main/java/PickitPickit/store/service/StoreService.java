package PickitPickit.store.service;

import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;

import java.util.List;

public interface StoreService {

    /**
     * 현재 위치 기반 주변 매장 조회
     *
     * @param latitude     사용자 위도
     * @param longitude    사용자 경도
     * @param radiusMeters 검색 반경
     * @param type         매장 유형
     * @return 주변 매장 목록
     */
    List<StoreResponse> getNearbyStores(double latitude, double longitude,
                                        int radiusMeters, StoreType type);

    /**
     * 매장 상세 조회
     *
     * @param storeId 매장 ID
     * @param userLat 사용자 위도
     * @param userLng 사용자 경도
     * @return 매장 상세 정보
     */
    StoreDetailResponse getStoreDetail(Long storeId, Double userLat, Double userLng);

    /**
     * 매장명 또는 주소 기반 매장 검색
     *
     * @param keyword 검색어
     * @param type    매장 유형
     * @param userLat 사용자 위도
     * @param userLng 사용자 경도
     * @param limit   조회 개수
     * @return 검색된 매장 목록
     */
    List<StoreResponse> searchStores(String keyword, StoreType type,
                                     Double userLat, Double userLng, int limit);
}