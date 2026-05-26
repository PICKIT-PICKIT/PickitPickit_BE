package PickitPickit.store.repository;

import PickitPickit.store.domain.StoreManager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreManagerRepository extends JpaRepository<StoreManager, Long> {

    boolean existsByStoreIdAndUserId(Long storeId, Long userId);
}
