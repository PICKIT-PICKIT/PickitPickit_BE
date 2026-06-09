package PickitPickit.store.repository;

import PickitPickit.store.domain.MapSourceType;
import PickitPickit.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsBySourceTypeAndSourcePlaceId(MapSourceType sourceType, String sourcePlaceId);

    @Query(value = """
            select *
            from stores s
            where s.latitude between :minLat and :maxLat
              and s.longitude between :minLng and :maxLng
              and (:storeType is null or s.store_type = :storeType)
              and (
                    6371000 * 2 * asin(
                        sqrt(
                            power(sin(radians(cast(s.latitude as double precision) - :lat) / 2), 2)
                            + cos(radians(:lat))
                            * cos(radians(cast(s.latitude as double precision)))
                            * power(sin(radians(cast(s.longitude as double precision) - :lng) / 2), 2)
                        )
                    )
                  ) <= :radiusMeters
            order by
              6371000 * 2 * asin(
                sqrt(
                    power(sin(radians(cast(s.latitude as double precision) - :lat) / 2), 2)
                    + cos(radians(:lat))
                    * cos(radians(cast(s.latitude as double precision)))
                    * power(sin(radians(cast(s.longitude as double precision) - :lng) / 2), 2)
                )
              ) asc
            """, nativeQuery = true)
    List<Store> findNearbyStores(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng,
            @Param("storeType") String storeType,
            @Param("radiusMeters") int radiusMeters
    );

    @Query(value = """
            select *
            from stores s
            where (:storeType is null or s.store_type = :storeType)
              and (
                  lower(s.name) like lower(concat('%', :keyword, '%'))
                  or lower(s.address) like lower(concat('%', :keyword, '%'))
              )
            order by s.name asc
            limit :limit
            """, nativeQuery = true)
    List<Store> searchByNameOrAddress(
            @Param("keyword") String keyword,
            @Param("storeType") String storeType,
            @Param("limit") int limit
    );

    @Query(value = """
            select *
            from stores s
            where (:storeType is null or s.store_type = :storeType)
              and (
                  lower(s.name) like lower(concat('%', :keyword, '%'))
                  or lower(s.address) like lower(concat('%', :keyword, '%'))
              )
            order by
              6371000 * 2 * asin(
                sqrt(
                    power(sin(radians(cast(s.latitude as double precision) - :lat) / 2), 2)
                    + cos(radians(:lat))
                    * cos(radians(cast(s.latitude as double precision)))
                    * power(sin(radians(cast(s.longitude as double precision) - :lng) / 2), 2)
                )
              ) asc
            limit :limit
            """, nativeQuery = true)
    List<Store> searchByNameOrAddressOrderByDistance(
            @Param("keyword") String keyword,
            @Param("storeType") String storeType,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("limit") int limit
    );
}
