package PickitPickit.search.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.search.domain.SearchLog;
import PickitPickit.search.domain.SearchTargetType;
import PickitPickit.search.dto.request.SearchLogCreateRequest;
import PickitPickit.search.dto.response.RecentSearchResponse;
import PickitPickit.search.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchLogServiceImpl implements SearchLogService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;

    private final SearchLogRepository searchLogRepository;

    @Override
    @Transactional
    public void saveSearchLog(SearchLogCreateRequest request) {
        SearchLog log = SearchLog.create(
                request.userId(),
                request.keyword(),
                request.targetType()
        );

        searchLogRepository.save(log);
    }

    @Override
    public List<RecentSearchResponse> getRecentSearches(Long userId, int limit) {
        validateUserId(userId);
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "limit은 1 이상 20 이하여야 합니다.");
        }

        return searchLogRepository.findRecentGroupedByUserId(userId, PageRequest.of(0, limit))
                .stream()
                .map(RecentSearchResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteRecentSearch(Long userId, String keyword, SearchTargetType targetType) {
        validateUserId(userId);
        validateTargetType(targetType);
        String normalizedKeyword = normalizeKeyword(keyword);

        searchLogRepository.deleteByUserIdAndKeywordAndTargetType(userId, normalizedKeyword, targetType);
    }

    @Override
    @Transactional
    public void clearRecentSearches(Long userId) {
        validateUserId(userId);
        searchLogRepository.deleteByUserId(userId);
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "userId는 필수이며 1 이상이어야 합니다.");
        }
    }

    private void validateTargetType(SearchTargetType targetType) {
        if (targetType == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "targetType은 필수입니다.");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "keyword는 필수입니다.");
        }

        String normalized = keyword.trim();
        if (normalized.length() > 255) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "keyword는 255자 이하여야 합니다.");
        }

        return normalized;
    }
}
