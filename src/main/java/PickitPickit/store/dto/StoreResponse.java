package PickitPickit.store.dto;

import PickitPickit.store.domain.Store;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse {

    /** 우리 DB PK */
    private final Long id;

    /** 외부 API 식별자 (공공데이터 MNG_NO / 카카오 place_id 등) */
    private final String sourcePlaceId;

    /** 매장명 */
    private final String name;

    /** 매장 유형 */
    private final StoreType type;

    /** 위도 */
    private final double latitude;

    /** 경도 */
    private final double longitude;

    /** 현재 위치로부터의 거리 (m) */
    private final int distance;

    /** 도로명 주소 */
    private final String address;

    /** 전화번호 */
    private final String contact;

    /** 카카오맵 상세 URL (PUBLIC_API 출처이면 null) */
    private final String kakaoDetailUrl;

    /**
     * DB 엔티티 → 응답 DTO 변환
     */
    public static StoreResponse from(Store store, int distanceMeters) {
        return StoreResponse.builder()
                .id(store.getId())
                .sourcePlaceId(store.getSourcePlaceId())
                .name(store.getName())
                .type(store.getStoreType())
                .latitude(store.getLatitude().doubleValue())
                .longitude(store.getLongitude().doubleValue())
                .distance(distanceMeters)
                .address(store.getAddress())
                .contact(store.getContact())
                .build();
    }
}
