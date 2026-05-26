package PickitPickit.store.repository;

import PickitPickit.store.domain.StoreProduct;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

    @EntityGraph(attributePaths = {"store", "item"})
    List<StoreProduct> findAllByStoreIdOrderByCreatedAtDesc(Long storeId);

    @EntityGraph(attributePaths = {"store", "item"})
    List<StoreProduct> findAllByIdIn(Collection<Long> ids);

    boolean existsByStoreIdAndItemId(Long storeId, Long itemId);

    @Query("select sp.store.id from StoreProduct sp where sp.id = :storeProductId")
    Optional<Long> findStoreIdById(@Param("storeProductId") Long storeProductId);
}
