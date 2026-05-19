package PickitPickit.store.repository;

import PickitPickit.store.domain.MapSourceType;
import PickitPickit.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findBySourceTypeAndSourcePlaceId(MapSourceType sourceType, String sourcePlaceId);

    boolean existsBySourceTypeAndSourcePlaceId(MapSourceType sourceType, String sourcePlaceId);

    /**
     * 매장명 또는 주소 기반 검색.
     * 위치 정보가 없을 때 사용.
     */
    @Query(value = """
            SELECT s.*
            FROM stores s
            WHERE (:storeType IS NULL OR s.store_type = :storeType)
              AND (
                    LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY s.name ASC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Store> searchByNameOrAddress(
            @Param("keyword") String keyword,
            @Param("storeType") String storeType,
            @Param("limit") int limit
    );

    /**
     * 매장명 또는 주소 기반 검색.
     * 위치 정보가 있을 때 현재 위치 기준 가까운 순으로 정렬.
     */
    @Query(value = """
            SELECT s.*
            FROM stores s
            WHERE (:storeType IS NULL OR s.store_type = :storeType)
              AND (
                    LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            ORDER BY (
                6371000 * acos(
                    LEAST(1.0, GREATEST(-1.0,
                        cos(radians(:lat)) * cos(radians(s.latitude))
                        * cos(radians(s.longitude) - radians(:lng))
                        + sin(radians(:lat)) * sin(radians(s.latitude))
                    ))
                )
            ) ASC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Store> searchByNameOrAddressOrderByDistance(
            @Param("keyword") String keyword,
            @Param("storeType") String storeType,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("limit") int limit
    );

    /**
     * 위도/경도 바운딩 박스 + 매장 유형 필터로 주변 매장 조회.
     * Haversine 공식으로 정확한 거리를 계산하고 반경 내 매장만 반환.
     * storeType 파라미터가 null이면 전체 유형을 조회합니다.
     */
    @Query(value = """
            SELECT s.*
            FROM stores s
            WHERE s.latitude BETWEEN :minLat AND :maxLat
              AND s.longitude BETWEEN :minLng AND :maxLng
              AND (:storeType IS NULL OR s.store_type = :storeType)
              AND (6371000 * acos(
                       LEAST(1.0, GREATEST(-1.0,
                           cos(radians(:lat)) * cos(radians(s.latitude))
                           * cos(radians(s.longitude) - radians(:lng))
                           + sin(radians(:lat)) * sin(radians(s.latitude))
                       ))
                   )) <= :radius
            ORDER BY (6371000 * acos(
                       LEAST(1.0, GREATEST(-1.0,
                           cos(radians(:lat)) * cos(radians(s.latitude))
                           * cos(radians(s.longitude) - radians(:lng))
                           + sin(radians(:lat)) * sin(radians(s.latitude))
                       ))
                   ))
            """,
            nativeQuery = true)
    List<Store> findNearbyStores(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng,
            @Param("storeType") String storeType,
            @Param("radius") int radius
    );
}