package PickitPickit.auth.repository;

import PickitPickit.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByUserId(Long userId);
}
