package PickitPickit.onboarding.repository;

import PickitPickit.onboarding.domain.InterestTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InterestTagRepository extends JpaRepository<InterestTag, Long> {

    List<InterestTag> findByActiveTrueOrderByDisplayOrderAsc();

    List<InterestTag> findByIdInAndActiveTrue(Collection<Long> ids);
}
