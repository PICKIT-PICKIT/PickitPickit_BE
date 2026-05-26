package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Store", description = "매장 조회 및 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    @Operation(
            summary = "현재 위치 기반 주변 매장 조회",
            description = """
                    사용자의 현재 위도/경도를 기준으로 반경 내 인형뽑기 & 가챠샵을 거리순으로 반환합니다.

                    - radius 허용값: 500 / 1000 / 3000 / 5000
                    - type 허용값: CLAW / GACHA / ALL
                    - 로그인 없이 조회 가능합니다.
                    """
    )
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getNearbyStores(
            @Parameter(description = "사용자 위도", required = true, example = "37.5665")
            @RequestParam double lat,

            @Parameter(description = "사용자 경도", required = true, example = "126.9780")
            @RequestParam double lng,

            @Parameter(description = "검색 반경(m)", example = "1000")
            @RequestParam(defaultValue = "1000") int radius,

            @Parameter(description = "매장 유형", example = "ALL")
            @RequestParam(defaultValue = "ALL") StoreType type
    ) {
        List<StoreResponse> stores = storeService.getNearbyStores(lat, lng, radius, type);
        return ApiResponse.success(SuccessStatus.NEARBY_STORES_FETCHED, stores);
    }

    @Operation(
            summary = "매장 검색",
            description = """
                    매장명 또는 주소 기준으로 매장을 검색합니다.

                    - keyword: 매장명 또는 주소 검색어
                    - type 허용값: CLAW / GACHA / ALL
                    - lat, lng를 함께 전달하면 가까운 순으로 정렬합니다.
                    - lat, lng를 생략하면 매장명 기준으로 정렬합니다.
                    - limit 허용값: 1~50
                    - 로그인 없이 조회 가능합니다.
                    """
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> searchStores(
            @Parameter(description = "검색어", required = true, example = "홍대")
            @RequestParam String keyword,

            @Parameter(description = "매장 유형", example = "ALL")
            @RequestParam(defaultValue = "ALL") StoreType type,

            @Parameter(description = "사용자 위도", example = "37.5665")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "사용자 경도", example = "126.9780")
            @RequestParam(required = false) Double lng,

            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<StoreResponse> stores = storeService.searchStores(keyword, type, lat, lng, limit);
        return ApiResponse.success(SuccessStatus.FETCHED, stores);
    }

    @Operation(
            summary = "매장 상세 조회",
            description = """
                    매장 상세 정보와 등록된 상품/재고/태그 목록을 조회합니다.

                    - lat, lng를 함께 전달하면 현재 위치 기준 거리를 계산합니다.
                    - lat, lng를 생략하면 distance는 0으로 반환됩니다.
                    - 로그인 없이 조회 가능합니다.
                    """
    )
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,

            @Parameter(description = "사용자 위도", example = "37.5665")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "사용자 경도", example = "126.9780")
            @RequestParam(required = false) Double lng
    ) {
        StoreDetailResponse response = storeService.getStoreDetail(storeId, lat, lng);
        return ApiResponse.success(SuccessStatus.FETCHED, response);
    }
}