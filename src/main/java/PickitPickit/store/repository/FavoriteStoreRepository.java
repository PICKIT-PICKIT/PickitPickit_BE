package PickitPickit.store.repository;

import PickitPickit.store.domain.FavoriteStore;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {

    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    Optional<FavoriteStore> findByUserIdAndStoreId(Long userId, Long storeId);

    @EntityGraph(attributePaths = {"store"})
    List<FavoriteStore> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}