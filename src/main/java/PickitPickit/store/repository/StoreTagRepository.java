package PickitPickit.store.repository;

import PickitPickit.store.domain.StoreTag;
import PickitPickit.store.domain.StoreTagSource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreTagRepository extends JpaRepository<StoreTag, Long> {

    @EntityGraph(attributePaths = {"tag"})
    @Query("""
            select st
            from StoreTag st
            join st.tag t
            where st.store.id = :storeId
            order by t.name asc
            """)
    List<StoreTag> findAllByStoreIdOrderByTagNameAsc(@Param("storeId") Long storeId);

    @EntityGraph(attributePaths = {"tag"})
    @Query("""
            select st
            from StoreTag st
            join st.tag t
            where st.store.id = :storeId
              and st.source = :source
            order by t.name asc
            """)
    List<StoreTag> findAllByStoreIdAndSourceOrderByTagNameAsc(
            @Param("storeId") Long storeId,
            @Param("source") StoreTagSource source
    );

    Optional<StoreTag> findByStoreIdAndTagId(Long storeId, Long tagId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from StoreTag st where st.store.id = :storeId")
    long deleteAllByStoreId(@Param("storeId") Long storeId);
}
