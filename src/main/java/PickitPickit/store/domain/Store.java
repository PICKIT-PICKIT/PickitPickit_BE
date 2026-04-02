package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
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
                @UniqueConstraint(
                        name = "uq_stores_source",
                        columnNames = {"source_type", "source_place_id"}
                )
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

    @Column(name = "source_place_id", length = 100)
    private String sourcePlaceId;

    @Column(nullable = false)
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

    @Column(name = "business_hours")
    private String businessHours;

    @Column(name = "main_image_url", length = 500)
    private String mainImageUrl;

    @Builder
    private Store(MapSourceType sourceType, String sourcePlaceId, String name,
                  StoreType storeType, String address, BigDecimal latitude, BigDecimal longitude,
                  String contact, String businessHours, String mainImageUrl) {
        this.sourceType = sourceType;
        this.sourcePlaceId = sourcePlaceId;
        this.name = name;
        this.storeType = storeType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contact = contact;
        this.businessHours = businessHours;
        this.mainImageUrl = mainImageUrl;
    }

    /**
     * 공공데이터 기반 매장 생성 팩토리 메서드
     */
    public static Store createFromPublicApi(String mngNo, String name, StoreType storeType,
                                             String address, BigDecimal latitude, BigDecimal longitude,
                                             String contact) {
        return Store.builder()
                .sourceType(MapSourceType.PUBLIC_API)
                .sourcePlaceId(mngNo)
                .name(name)
                .storeType(storeType)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .contact(contact)
                .build();
    }

    /**
     * 카카오맵 기반 매장 생성 팩토리 메서드
     */
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
}
