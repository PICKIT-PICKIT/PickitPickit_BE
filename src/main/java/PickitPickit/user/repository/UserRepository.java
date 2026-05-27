package PickitPickit.user.repository;

import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(String kakaoId);

    Optional<User> findByKakaoIdAndStatus(String kakaoId, UserStatus status);

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    boolean existsByIdAndStatus(Long id, UserStatus status);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long id);
}