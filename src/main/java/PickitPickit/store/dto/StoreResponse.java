package PickitPickit.store.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse {

    /** 카카오 place_id (리뷰 연동 시 storeId 로 사용) */
    private final String placeId;

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
    private final String phone;

    /** 카카오맵 상세 URL */
    private final String kakaoDetailUrl;
}
