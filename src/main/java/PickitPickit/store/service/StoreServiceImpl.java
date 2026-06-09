package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.domain.Store;
import PickitPickit.store.domain.StoreProduct;
import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.dto.TagResponse;
import PickitPickit.store.repository.StoreProductRepository;
import PickitPickit.store.repository.StoreProductTagRepository;
import PickitPickit.store.repository.StoreRepository;
import PickitPickit.store.repository.StoreTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private static final Set<Integer> ALLOWED_RADIUS = Set.of(500, 1000, 3000, 5000);
    private static final double METERS_PER_DEGREE_LAT = 111_320.0;
    private static final int MIN_SEARCH_LIMIT = 1;
    private static final int MAX_SEARCH_LIMIT = 50;

    private final StoreRepository storeRepository;
    private final StoreProductRepository storeProductRepository;
    private final StoreTagRepository storeTagRepository;
    private final StoreProductTagRepository storeProductTagRepository;

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
                .map(store -> StoreResponse.from(store, calculateDistance(
                        latitude,
                        longitude,
                        store.getLatitude().doubleValue(),
                        store.getLongitude().doubleValue()
                )))
                .toList();
    }

    @Override
    public StoreDetailResponse getStoreDetail(Long storeId, Double userLat, Double userLng) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장을 찾을 수 없습니다."));

        int distance = resolveDistance(store, userLat, userLng);
        StoreResponse storeResponse = StoreResponse.from(store, distance);

        List<StoreProduct> storeProducts = storeProductRepository.findAllByStoreIdOrderByCreatedAtDesc(storeId);
        List<Long> storeProductIds = storeProducts.stream()
                .map(StoreProduct::getId)
                .toList();

        Map<Long, List<TagResponse>> productTagMap = getProductTagMap(storeProductIds);

        List<StoreDetailResponse.ProductInfo> products = storeProducts.stream()
                .map(sp -> StoreDetailResponse.ProductInfo.from(
                        sp,
                        productTagMap.getOrDefault(sp.getId(), List.of())
                ))
                .toList();

        List<TagResponse> storeTags = storeTagRepository.findAllByStoreIdOrderByTagNameAsc(storeId)
                .stream()
                .map(StoreTag::getTag)
                .map(TagResponse::from)
                .toList();

        int totalStockQuantity = storeProducts.stream()
                .map(StoreProduct::getStockQuantity)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum();

        return new StoreDetailResponse(
                storeResponse,
                products.size(),
                totalStockQuantity,
                storeTags,
                products
        );
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
                .map(store -> StoreResponse.from(store, hasLocation
                        ? calculateDistance(userLat, userLng, store.getLatitude().doubleValue(), store.getLongitude().doubleValue())
                        : 0))
                .toList();
    }

    private Map<Long, List<TagResponse>> getProductTagMap(Collection<Long> storeProductIds) {
        if (storeProductIds == null || storeProductIds.isEmpty()) {
            return Map.of();
        }

        return storeProductTagRepository.findAllByStoreProductIdIn(storeProductIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getStoreProduct().getId(),
                        Collectors.mapping(tag -> TagResponse.from(tag.getTag()), Collectors.toList())
                ));
    }

    private int resolveDistance(Store store, Double userLat, Double userLng) {
        if ((userLat == null) != (userLng == null)) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "lat, lng는 둘 다 입력하거나 둘 다 생략해야 합니다.");
        }

        if (userLat == null) {
            return 0;
        }

        validateCoordinate(userLat, userLng);
        return calculateDistance(
                userLat,
                userLng,
                store.getLatitude().doubleValue(),
                store.getLongitude().doubleValue()
        );
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
