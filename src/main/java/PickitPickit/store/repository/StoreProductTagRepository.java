package PickitPickit.store.repository;

import PickitPickit.store.domain.StoreProductTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StoreProductTagRepository extends JpaRepository<StoreProductTag, Long> {

    @EntityGraph(attributePaths = {"tag", "storeProduct"})
    @Query("""
            select spt
            from StoreProductTag spt
            join spt.storeProduct sp
            join spt.tag t
            where sp.id in :storeProductIds
            order by t.name asc
            """)
    List<StoreProductTag> findAllByStoreProductIdIn(@Param("storeProductIds") Collection<Long> storeProductIds);

    @EntityGraph(attributePaths = {"tag"})
    @Query("""
            select spt
            from StoreProductTag spt
            join spt.tag t
            where spt.storeProduct.id = :storeProductId
            order by t.name asc
            """)
    List<StoreProductTag> findAllByStoreProductIdOrderByTagNameAsc(@Param("storeProductId") Long storeProductId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StoreProductTag spt where spt.storeProduct.id = :storeProductId")
    long deleteAllByStoreProductId(@Param("storeProductId") Long storeProductId);

    @Query("""
            select count(spt) > 0
            from StoreProductTag spt
            join spt.storeProduct sp
            where sp.store.id = :storeId
              and spt.tag.id = :tagId
            """)
    boolean existsByStoreIdAndTagId(
            @Param("storeId") Long storeId,
            @Param("tagId") Long tagId
    );
}
