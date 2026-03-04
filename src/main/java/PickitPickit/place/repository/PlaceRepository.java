package PickitPickit.place.repository;

import PickitPickit.place.domain.Place;
import PickitPickit.place.domain.PlaceCategory;
import PickitPickit.place.domain.PlaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("""
        select p
        from Place p
        where p.status = :status
          and (:category is null or p.category = :category)
          and p.lat between :minLat and :maxLat
          and p.lng between :minLng and :maxLng
        """)
    List<Place> findInBounds(
            @Param("status") PlaceStatus status,
            @Param("category") PlaceCategory category,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );
}