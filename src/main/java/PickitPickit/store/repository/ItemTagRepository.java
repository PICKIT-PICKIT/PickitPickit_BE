package PickitPickit.store.repository;

import PickitPickit.store.domain.ItemTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ItemTagRepository extends JpaRepository<ItemTag, Long> {

    @EntityGraph(attributePaths = {"tag"})
    @Query("""
            select it
            from ItemTag it
            join it.tag t
            where it.item.id = :itemId
            order by t.name asc
            """)
    List<ItemTag> findAllByItemIdOrderByTagNameAsc(@Param("itemId") Long itemId);

    @EntityGraph(attributePaths = {"tag", "item"})
    @Query("""
            select it
            from ItemTag it
            join it.item i
            join it.tag t
            where i.id in :itemIds
            order by t.name asc
            """)
    List<ItemTag> findAllByItemIdIn(@Param("itemIds") Collection<Long> itemIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ItemTag it where it.item.id = :itemId")
    long deleteAllByItemId(@Param("itemId") Long itemId);
}
