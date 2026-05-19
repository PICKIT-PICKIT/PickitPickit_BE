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
     * 위도 1도 ≈ 111,320m
     */
    private static final double METERS_PER_DEGREE_LAT = 111_320.0;

    private static final int MIN_SEARCH_LIMIT = 1;
    private static final int MAX_SEARCH_LIMIT = 50;

    private final StoreRepository storeRepository;
    private final StoreProductRepository storeProductRepository;

    @Override
    public List<StoreResponse> getNearbyStores(double latitude, double longitude,
                                               int radiusMeters, StoreType type) {
        validateCoordinate(latitude, longitude);

        if (!ALLOWED_RADIUS.contains(radiusMeters)) {
            throw new ApiException(ErrorStatus.INVALID_RADIUS);
        }

        StoreType safeType = type == null ? StoreType.ALL : type;

        BigDecimal deltaLat = BigDecimal.valueOf(radiusMeters / METERS_PER_DEGREE_LAT);
        double metersPerDegreeLng = METERS_PER_DEGREE_LAT * Math.cos(Math.toRadians(latitude));
        BigDecimal deltaLng = BigDecimal.valueOf(radiusMeters / metersPerDegreeLng);

        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lng = BigDecimal.valueOf(longitude);

        BigDecimal minLat = lat.subtract(deltaLat);
        BigDecimal maxLat = lat.add(deltaLat);
        BigDecimal minLng = lng.subtract(deltaLng);
        BigDecimal maxLng = lng.add(deltaLng);

        String storeTypeStr = safeType == StoreType.ALL ? null : safeType.name();

        List<Store> stores = storeRepository.findNearbyStores(
                latitude,
                longitude,
                minLat,
                maxLat,
                minLng,
                maxLng,
                storeTypeStr,
                radiusMeters
        );

        return stores.stream()
                .map(store -> {
                    int distance = calculateDistance(
                            latitude,
                            longitude,
                            store.getLatitude().doubleValue(),
                            store.getLongitude().doubleValue()
                    );

                    return StoreResponse.from(store, distance);
                })
                .toList();
    }

    @Override
    public StoreDetailResponse getStoreDetail(Long storeId, Double userLat, Double userLng) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

        int distance = 0;

        if ((userLat == null) != (userLng == null)) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "lat, lng는 둘 다 입력하거나 둘 다 생략해야 합니다.");
        }

        if (userLat != null && userLng != null) {
            validateCoordinate(userLat, userLng);

            distance = calculateDistance(
                    userLat,
                    userLng,
                    store.getLatitude().doubleValue(),
                    store.getLongitude().doubleValue()
            );
        }

        StoreResponse storeResponse = StoreResponse.from(store, distance);

        List<StoreDetailResponse.ProductInfo> products = storeProductRepository.findAllByStoreId(storeId)
                .stream()
                .map(StoreDetailResponse.ProductInfo::from)
                .toList();

        return new StoreDetailResponse(storeResponse, products);
    }

    @Override
    public List<StoreResponse> searchStores(String keyword, StoreType type,
                                            Double userLat, Double userLng, int limit) {
        String normalizedKeyword = normalizeKeyword(keyword);
        validateSearchLimit(limit);

        StoreType safeType = type == null ? StoreType.ALL : type;
        String storeTypeStr = safeType == StoreType.ALL ? null : safeType.name();

        if ((userLat == null) != (userLng == null)) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "lat, lng는 둘 다 입력하거나 둘 다 생략해야 합니다.");
        }

        boolean hasLocation = userLat != null && userLng != null;

        List<Store> stores;

        if (hasLocation) {
            validateCoordinate(userLat, userLng);

            stores = storeRepository.searchByNameOrAddressOrderByDistance(
                    normalizedKeyword,
                    storeTypeStr,
                    userLat,
                    userLng,
                    limit
            );
        } else {
            stores = storeRepository.searchByNameOrAddress(
                    normalizedKeyword,
                    storeTypeStr,
                    limit
            );
        }

        return stores.stream()
                .map(store -> {
                    int distance = 0;

                    if (hasLocation) {
                        distance = calculateDistance(
                                userLat,
                                userLng,
                                store.getLatitude().doubleValue(),
                                store.getLongitude().doubleValue()
                        );
                    }

                    return StoreResponse.from(store, distance);
                })
                .toList();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색어(keyword)는 필수입니다.");
        }

        String normalized = keyword.trim();

        if (normalized.length() > 255) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색어(keyword)는 255자 이하여야 합니다.");
        }

        return normalized;
    }

    private void validateSearchLimit(int limit) {
        if (limit < MIN_SEARCH_LIMIT || limit > MAX_SEARCH_LIMIT) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "limit은 1 이상 50 이하여야 합니다.");
        }
    }

    private void validateCoordinate(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "위도(lat)는 -90 이상 90 이하여야 합니다.");
        }

        if (longitude < -180 || longitude > 180) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "경도(lng)는 -180 이상 180 이하여야 합니다.");
        }
    }

    /**
     * Haversine 공식으로 두 좌표 간 거리 계산.
     *
     * @return 거리(m)
     */
    private int calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6_371_000;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) Math.round(earthRadius * c);
    }
}