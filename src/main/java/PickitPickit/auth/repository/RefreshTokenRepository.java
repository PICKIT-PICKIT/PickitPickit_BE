package PickitPickit.auth.repository;

import PickitPickit.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken r where r.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);
}
