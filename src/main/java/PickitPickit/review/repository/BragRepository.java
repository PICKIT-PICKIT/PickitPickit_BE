package PickitPickit.review.repository;

import PickitPickit.review.domain.Brag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BragRepository extends JpaRepository<Brag, Long> {

    Optional<Brag> findByIdAndUserId(Long id, Long userId);

    List<Brag> findAllByOrderByCreatedAtDesc();
}