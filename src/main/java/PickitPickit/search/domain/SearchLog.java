package PickitPickit.search.domain;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "search_logs",
        indexes = {
                @Index(name = "idx_search_logs_user_searched_at", columnList = "user_id, searched_at"),
                @Index(name = "idx_search_logs_keyword", columnList = "keyword")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private SearchTargetType targetType;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @Builder
    private SearchLog(Long userId, String keyword, SearchTargetType targetType, LocalDateTime searchedAt) {
        validateUserId(userId);
        this.userId = userId;
        this.keyword = normalizeKeyword(keyword);
        this.targetType = validateTargetType(targetType);
        this.searchedAt = searchedAt != null ? searchedAt : LocalDateTime.now();
    }

    public static SearchLog create(Long userId, String keyword, SearchTargetType targetType) {
        return SearchLog.builder()
                .userId(userId)
                .keyword(keyword)
                .targetType(targetType)
                .searchedAt(LocalDateTime.now())
                .build();
    }

    private static void validateUserId(Long userId) {
        if (userId != null && userId <= 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "userId는 1 이상이어야 합니다.");
        }
    }

    private static SearchTargetType validateTargetType(SearchTargetType targetType) {
        if (targetType == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색 대상 유형(targetType)은 필수입니다.");
        }
        return targetType;
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색어(keyword)는 필수입니다.");
        }

        String normalized = keyword.trim();
        if (normalized.length() > 255) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "검색어(keyword)는 255자 이하여야 합니다.");
        }

        return normalized;
    }
}
