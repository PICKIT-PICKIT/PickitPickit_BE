package PickitPickit.search.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.search.domain.SearchTargetType;
import PickitPickit.search.dto.request.SearchLogCreateRequest;
import PickitPickit.search.dto.response.RecentSearchResponse;
import PickitPickit.search.service.SearchLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Search Log", description = "검색 로그 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search-logs")
public class SearchLogController {

    private final SearchLogService searchLogService;

    @Operation(summary = "검색 로그 저장", description = "검색 실행 시 검색어 로그를 저장합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveSearchLog(
            @Valid @RequestBody SearchLogCreateRequest request
    ) {
        searchLogService.saveSearchLog(request);
        return ApiResponse.success(SuccessStatus.CREATED);
    }

    @Operation(
            summary = "최근 검색 조회",
            description = "검색창 진입 시 사용자의 최근 검색어를 최신순으로 조회합니다. 같은 검색어+대상 조합은 가장 최근 1건만 노출됩니다."
    )
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RecentSearchResponse>>> getRecentSearches(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "조회 개수 (1~20), 기본값 10", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, searchLogService.getRecentSearches(userId, limit));
    }

    @Operation(
            summary = "최근 검색 개별 삭제",
            description = "검색창의 x 버튼 클릭 시 해당 검색어를 삭제합니다. 동일 keyword+targetType 이력은 함께 제거됩니다."
    )
    @DeleteMapping("/recent")
    public ResponseEntity<ApiResponse<Void>> deleteRecentSearch(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "삭제할 검색어", required = true, example = "산리오")
            @RequestParam String keyword,
            @Parameter(description = "검색 대상 유형(STORE/ITEM/ALL)", required = true, example = "ALL")
            @RequestParam SearchTargetType targetType
    ) {
        searchLogService.deleteRecentSearch(userId, keyword, targetType);
        return ApiResponse.success(SuccessStatus.DELETED);
    }

    @Operation(
            summary = "최근 검색 전체 삭제",
            description = "검색창의 모두 지우기 클릭 시 해당 사용자의 최근 검색어를 전체 삭제합니다."
    )
    @DeleteMapping("/recent/all")
    public ResponseEntity<ApiResponse<Void>> clearRecentSearches(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId
    ) {
        searchLogService.clearRecentSearches(userId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }
}
