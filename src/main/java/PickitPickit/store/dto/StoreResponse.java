package PickitPickit.store.dto;

import PickitPickit.store.domain.Store;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse {

    private final Long id;
    private final String sourcePlaceId;
    private final String name;
    private final StoreType type;
    private final double latitude;
    private final double longitude;
    private final int distance;
    private final String address;
    private final String contact;
    private final String businessHours;
    private final String mainImageUrl;
    private final String kakaoDetailUrl;

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
                .businessHours(store.getBusinessHours())
                .mainImageUrl(store.getMainImageUrl())
                .build();
    }
}
