package PickitPickit.onboarding.repository;

import PickitPickit.onboarding.domain.UserInterestTag;
import PickitPickit.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserInterestTagRepository extends JpaRepository<UserInterestTag, Long> {

    List<UserInterestTag> findByUserId(Long userId);

    void deleteByUser(User user);

    boolean existsByUserId(Long userId);
}
