package PickitPickit.store.repository;

import PickitPickit.store.domain.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {

    List<StoreProduct> findAllByStoreId(Long storeId);
}
