package PickitPickit.search.dto.response;

import PickitPickit.search.domain.SearchTargetType;

import java.time.LocalDateTime;

public record RecentSearchProjection(
        String keyword,
        SearchTargetType targetType,
        LocalDateTime searchedAt
) {
}
