package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
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

@Tag(name = "Store", description = "주변 매장 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    @Operation(
            summary = "현재 위치 기반 주변 매장 조회",
            description = """
                    사용자의 현재 위도/경도를 기준으로 반경 내 인형뽑기 & 가챠샵을 거리순으로 반환합니다.
                    - radius 허용값: 500 / 1000 / 3000 / 5000 (m)
                    - type 허용값: CLAW(인형뽑기) / GACHA(가챠샵) / ALL(전체)
                    """
    )
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getNearbyStores(
            @Parameter(description = "사용자 위도 (예: 37.5665)", required = true, example = "37.5665")
            @RequestParam double lat,
            @Parameter(description = "사용자 경도 (예: 126.9780)", required = true, example = "126.9780")
            @RequestParam double lng,
            @Parameter(description = "검색 반경 (m), 기본값 1000. 허용: 500 | 1000 | 3000 | 5000", example = "1000")
            @RequestParam(defaultValue = "1000") int radius,
            @Parameter(description = "매장 유형. 기본값 ALL. 허용: CLAW | GACHA | ALL", example = "ALL")
            @RequestParam(defaultValue = "ALL") StoreType type
    ) {
        List<StoreResponse> stores = storeService.getNearbyStores(lat, lng, radius, type);
        return ApiResponse.success(SuccessStatus.NEARBY_STORES_FETCHED, stores);
    }
}
