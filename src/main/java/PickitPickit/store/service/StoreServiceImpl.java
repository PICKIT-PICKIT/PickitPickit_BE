package PickitPickit.store.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.client.KakaoKeywordResponse;
import PickitPickit.store.client.KakaoLocalClient;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final Set<Integer> ALLOWED_RADIUS = Set.of(500, 1000, 3000, 5000);

    /** 카카오 키워드 - 인형뽑기 */
    private static final String KEYWORD_CLAW = "인형뽑기";

    /** 카카오 키워드 - 가챠샵 */
    private static final String KEYWORD_GACHA = "가챠샵";

    private final KakaoLocalClient kakaoLocalClient;

    @Override
    public List<StoreResponse> getNearbyStores(double latitude, double longitude,
                                               int radiusMeters, StoreType type) {
        // 허용된 반경 값인지 검증
        if (!ALLOWED_RADIUS.contains(radiusMeters)) {
            throw new ApiException(ErrorStatus.INVALID_RADIUS);
        }

        // 매장 유형에 따라 키워드 결정
        List<String> keywords = switch (type) {
            case CLAW  -> List.of(KEYWORD_CLAW);
            case GACHA -> List.of(KEYWORD_GACHA);
            case ALL   -> List.of(KEYWORD_CLAW, KEYWORD_GACHA);
        };

        // 키워드별 검색 후 placeId 기준으로 중복 제거
        Map<String, StoreResponse> deduped = new LinkedHashMap<>();

        for (String keyword : keywords) {
            StoreType assignedType = KEYWORD_GACHA.equals(keyword) ? StoreType.GACHA : StoreType.CLAW;
            List<StoreResponse> results = fetchAll(keyword, latitude, longitude, radiusMeters, assignedType);
            for (StoreResponse store : results) {
                deduped.putIfAbsent(store.getPlaceId(), store);
            }
        }

        // 거리 오름차순 정렬
        return deduped.values().stream()
                .sorted(Comparator.comparingInt(StoreResponse::getDistance))
                .collect(Collectors.toList());
    }

    /**
     * 카카오 API 페이지네이션을 통해 모든 결과를 수집
     */
    private List<StoreResponse> fetchAll(String keyword, double latitude, double longitude,
                                         int radiusMeters, StoreType type) {
        List<StoreResponse> result = new ArrayList<>();
        int page = 1;

        while (true) {
            KakaoKeywordResponse response =
                    kakaoLocalClient.searchByKeyword(keyword, latitude, longitude, radiusMeters, page);

            if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                break;
            }

            response.getDocuments().stream()
                    .map(doc -> toStoreResponse(doc, type))
                    .forEach(result::add);

            // 마지막 페이지이거나 최대 45페이지 도달 시 중단
            if (response.getMeta().isEnd() || page >= 45) {
                break;
            }
            page++;
        }

        return result;
    }

    private StoreResponse toStoreResponse(KakaoKeywordResponse.Document doc, StoreType type) {
        String address = (doc.getRoadAddressName() != null && !doc.getRoadAddressName().isBlank())
                ? doc.getRoadAddressName()
                : doc.getAddressName();

        return StoreResponse.builder()
                .placeId(doc.getId())
                .name(doc.getPlaceName())
                .type(type)
                .latitude(parseDouble(doc.getY()))
                .longitude(parseDouble(doc.getX()))
                .distance(parseInt(doc.getDistance()))
                .address(address)
                .phone(doc.getPhone())
                .kakaoDetailUrl(doc.getPlaceUrl())
                .build();
    }

    private double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(String value) {
        try {
            return value != null ? Integer.parseInt(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
