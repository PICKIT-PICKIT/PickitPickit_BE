package PickitPickit.search.repository;

import PickitPickit.search.domain.SearchLog;
import PickitPickit.search.domain.SearchTargetType;
import PickitPickit.search.dto.response.RecentSearchProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query("""
            select new PickitPickit.search.dto.response.RecentSearchProjection(
                s.keyword,
                s.targetType,
                max(s.searchedAt)
            )
            from SearchLog s
            where s.userId = :userId
            group by s.keyword, s.targetType
            order by max(s.searchedAt) desc
            """)
    List<RecentSearchProjection> findRecentGroupedByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    long deleteByUserIdAndKeywordAndTargetType(Long userId, String keyword, SearchTargetType targetType);

    long deleteByUserId(Long userId);
}
