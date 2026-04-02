package PickitPickit.search.service;

import PickitPickit.search.domain.SearchTargetType;
import PickitPickit.search.dto.request.SearchLogCreateRequest;
import PickitPickit.search.dto.response.RecentSearchResponse;

import java.util.List;

public interface SearchLogService {

    void saveSearchLog(SearchLogCreateRequest request);

    List<RecentSearchResponse> getRecentSearches(Long userId, int limit);

    void deleteRecentSearch(Long userId, String keyword, SearchTargetType targetType);

    void clearRecentSearches(Long userId);
}
