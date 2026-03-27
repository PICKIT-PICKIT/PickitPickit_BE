package PickitPickit.review.repository;

import PickitPickit.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    Optional<Review> findByIdAndUserId(Long id, Long userId);

    List<Review> findAllByStoreIdOrderByCreatedAtDesc(Long storeId);

    long countByStoreId(Long storeId);

    @Query("select avg(r.rating) from Review r where r.storeId = :storeId")
    Double findAverageRatingByStoreId(Long storeId);

    @Query("select avg(r.difficulty) from Review r where r.storeId = :storeId and r.difficulty is not null")
    Double findAverageDifficultyByStoreId(Long storeId);
}