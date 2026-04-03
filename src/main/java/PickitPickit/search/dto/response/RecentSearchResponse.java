package PickitPickit.search.dto.response;

import PickitPickit.search.domain.SearchTargetType;

import java.time.LocalDateTime;

public record RecentSearchResponse(
        String keyword,
        SearchTargetType targetType,
        LocalDateTime searchedAt
) {
    public static RecentSearchResponse from(RecentSearchProjection projection) {
        return new RecentSearchResponse(
                projection.keyword(),
                projection.targetType(),
                projection.searchedAt()
        );
    }
}
