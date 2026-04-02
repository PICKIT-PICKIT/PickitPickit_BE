package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.Store;
import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.repository.StoreProductRepository;
import PickitPickit.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private static final Set<Integer> ALLOWED_RADIUS = Set.of(500, 1000, 3000, 5000);

    /**
     * 위도 1도 ≈ 111,320m → 바운딩 박스 계산에 사용
     */
    private static final double METERS_PER_DEGREE_LAT = 111_320.0;

    private final StoreRepository storeRepository;
    private final StoreProductRepository storeProductRepository;

    @Override
    public List<StoreResponse> getNearbyStores(double latitude, double longitude,
                                                int radiusMeters, StoreType type) {
        if (!ALLOWED_RADIUS.contains(radiusMeters)) {
            throw new ApiException(ErrorStatus.INVALID_RADIUS);
        }

        // 바운딩 박스 계산 (인덱스 활용을 위한 사전 필터)
        BigDecimal deltaLat = BigDecimal.valueOf(radiusMeters / METERS_PER_DEGREE_LAT);
        double metersPerDegreeLng = METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(latitude));
        BigDecimal deltaLng = BigDecimal.valueOf(radiusMeters / metersPerDegreeLng);

        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lng = BigDecimal.valueOf(longitude);

        BigDecimal minLat = lat.subtract(deltaLat);
        BigDecimal maxLat = lat.add(deltaLat);
        BigDecimal minLng = lng.subtract(deltaLng);
        BigDecimal maxLng = lng.add(deltaLng);

        // ALL이면 storeType = null → 전체 조회
        String storeTypeStr = (type == StoreType.ALL) ? null : type.name();

        List<Store> stores = storeRepository.findNearbyStores(
                latitude, longitude, minLat, maxLat, minLng, maxLng, storeTypeStr, radiusMeters
        );

        return stores.stream()
                .map(store -> {
                    int distance = calculateDistance(latitude, longitude,
                            store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
                    return StoreResponse.from(store, distance);
                })
                .toList();
    }

    @Override
    public StoreDetailResponse getStoreDetail(Long storeId, Double userLat, Double userLng) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

        int distance = 0;
        if (userLat != null && userLng != null) {
            distance = calculateDistance(userLat, userLng,
                    store.getLatitude().doubleValue(), store.getLongitude().doubleValue());
        }

        StoreResponse storeResponse = StoreResponse.from(store, distance);

        List<StoreDetailResponse.ProductInfo> products = storeProductRepository.findAllByStoreId(storeId)
                .stream()
                .map(StoreDetailResponse.ProductInfo::from)
                .toList();

        return new StoreDetailResponse(storeResponse, products);
    }

    /**
     * Haversine 공식으로 두 좌표 간 거리 계산 (미터 단위)
     */
    private int calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6_371_000; // 지구 반지름 (m)

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) Math.round(earthRadius * c);
    }
}
