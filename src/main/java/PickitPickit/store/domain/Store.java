package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.dto.StoreType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "stores",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_stores_source", columnNames = {"source_type", "source_place_id"})
        },
        indexes = {
                @Index(name = "idx_stores_name", columnList = "name"),
                @Index(name = "idx_stores_address", columnList = "address"),
                @Index(name = "idx_stores_type", columnList = "store_type"),
                @Index(name = "idx_stores_lat_lng", columnList = "latitude, longitude")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private MapSourceType sourceType;

    /**
     * PUBLIC_API는 regionCode:mngNo 형태로 저장한다.
     * MNG_NO 단독 사용 시 자치단체별 중복이 발생할 수 있다.
     */
    @Column(name = "source_place_id", nullable = false, length = 150)
    private String sourcePlaceId;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_type", nullable = false, length = 20)
    private StoreType storeType;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 50)
    private String contact;

    @Column(name = "business_hours", length = 100)
    private String businessHours;

    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;

    @Builder
    private Store(MapSourceType sourceType, String sourcePlaceId, String name,
                  StoreType storeType, String address, BigDecimal latitude, BigDecimal longitude,
                  String contact, String businessHours, String mainImageUrl) {
        this.sourceType = validateSourceType(sourceType);
        this.sourcePlaceId = normalizeRequired(sourcePlaceId, "외부 매장 식별자");
        this.name = normalizeRequired(name, "매장명");
        this.storeType = validateStoreType(storeType);
        this.address = normalizeRequired(address, "주소");
        this.latitude = validateLatitude(latitude);
        this.longitude = validateLongitude(longitude);
        this.contact = normalizeNullable(contact);
        this.businessHours = normalizeNullable(businessHours);
        this.mainImageUrl = normalizeNullable(mainImageUrl);
    }

    public static Store createFromPublicApi(String sourcePlaceId, String name, StoreType storeType,
                                            String address, BigDecimal latitude, BigDecimal longitude,
                                            String contact) {
        return Store.builder()
                .sourceType(MapSourceType.PUBLIC_API)
                .sourcePlaceId(sourcePlaceId)
                .name(name)
                .storeType(storeType)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .contact(contact)
                .build();
    }

    public static Store createFromKakaoMap(String placeId, String name, StoreType storeType,
                                           String address, BigDecimal latitude, BigDecimal longitude,
                                           String phone) {
        return Store.builder()
                .sourceType(MapSourceType.KAKAO_MAP)
                .sourcePlaceId(placeId)
                .name(name)
                .storeType(storeType)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .contact(phone)
                .build();
    }

    public void updateDisplayInfo(String businessHours, String mainImageUrl, String contact) {
        this.businessHours = normalizeNullable(businessHours);
        this.mainImageUrl = normalizeNullable(mainImageUrl);
        this.contact = normalizeNullable(contact);
    }

    private static MapSourceType validateSourceType(MapSourceType sourceType) {
        if (sourceType == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "매장 출처는 필수입니다.");
        }
        return sourceType;
    }

    private static StoreType validateStoreType(StoreType storeType) {
        if (storeType == null || storeType == StoreType.ALL) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "매장 유형은 CLAW 또는 GACHA여야 합니다.");
        }
        return storeType;
    }

    private static BigDecimal validateLatitude(BigDecimal latitude) {
        if (latitude == null || latitude.doubleValue() < -90 || latitude.doubleValue() > 90) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "위도 값이 올바르지 않습니다.");
        }
        return latitude;
    }

    private static BigDecimal validateLongitude(BigDecimal longitude) {
        if (longitude == null || longitude.doubleValue() < -180 || longitude.doubleValue() > 180) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "경도 값이 올바르지 않습니다.");
        }
        return longitude;
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, fieldName + "은 필수입니다.");
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
