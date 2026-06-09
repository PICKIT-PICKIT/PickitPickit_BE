package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.StoreDetailResponse;
import PickitPickit.store.dto.StoreRecommendationResponse;
import PickitPickit.store.dto.StoreResponse;
import PickitPickit.store.dto.StoreType;
import PickitPickit.store.service.StoreRecommendationService;
import PickitPickit.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@Tag(name = "Store", description = "매장 조회 및 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
@SecurityRequirement(name = "bearerAuth")
public class StoreController {

    private final StoreService storeService;
    private final StoreRecommendationService storeRecommendationService;

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
            summary = "Claude 기반 AI 매장 추천",
            description = """
                    로그인 사용자의 관심 태그, 검색어, 현재 위치를 기준으로
                    Claude API가 후보 매장 중 추천 매장을 선정합니다.

                    프론트 연동 가이드:
                    - 검색창 검색어(keyword)와 현재 위치(lat, lng)만 필수로 전달합니다.
                    - 검색 반경은 프론트에서 전달하지 않습니다. 서버가 내부적으로 3km 반경 후보를 사용합니다.
                    - limit 기본값은 3이며, 메인페이지 추천 카드 3개 노출을 기준으로 설계했습니다.
                    - 응답 배열 순서는 추천 우선순위입니다. 그대로 화면에 렌더링하면 됩니다.
                    - Claude API 키가 없거나 외부 호출이 실패해도 500을 반환하지 않고 후보 매장 fallback을 반환합니다.
                    - 주변 후보가 없으면 data는 빈 배열([])입니다.

                    요청 예시:
                    GET /api/stores/recommendations?keyword=포켓몬&lat=37.5665&lng=126.9780&type=ALL&limit=3
                    Authorization: Bearer {accessToken}

                    - keyword: 검색창에 입력한 검색어
                    - lat, lng: 사용자 현재 위치
                    - type 허용값: CLAW / GACHA / ALL
                    - limit 허용값: 1~10, 기본값 3
                    - Claude 호출 실패 시 후보 매장을 거리순/검색순으로 반환합니다.
                    - 로그인이 필요합니다.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "AI 추천 매장 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Claude 추천 성공 또는 fallback 응답",
                                    value = """
                                            {
                                              "code": "FETCHED",
                                              "message": "데이터 조회 성공",
                                              "data": [
                                                {
                                                  "store": {
                                                    "id": 1,
                                                    "sourcePlaceId": "manual-1",
                                                    "name": "피카츄 뽑기",
                                                    "type": "CLAW",
                                                    "latitude": 37.5665,
                                                    "longitude": 126.978,
                                                    "distance": 420,
                                                    "address": "서울시 중구 테스트로 1",
                                                    "contact": "02-123-4567",
                                                    "businessHours": "10:00-22:00",
                                                    "mainImageUrl": "https://example.com/store.png"
                                                  },
                                                  "recommendationReason": "관심 태그인 포켓몬과 가까운 위치를 기준으로 추천합니다."
                                                },
                                                {
                                                  "store": {
                                                    "id": 2,
                                                    "sourcePlaceId": "manual-2",
                                                    "name": "홍대 가챠존",
                                                    "type": "GACHA",
                                                    "latitude": 37.5571,
                                                    "longitude": 126.9245,
                                                    "distance": 980,
                                                    "address": "서울시 마포구 테스트로 2"
                                                  },
                                                  "recommendationReason": "검색어와 관련 있는 가챠 상품이 있는 가까운 매장입니다."
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<StoreRecommendationResponse>>> recommendStores(
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "검색어", required = true, example = "포켓몬")
            @RequestParam String keyword,

            @Parameter(description = "사용자 위도", required = true, example = "37.5665")
            @RequestParam double lat,

            @Parameter(description = "사용자 경도", required = true, example = "126.9780")
            @RequestParam double lng,

            @Parameter(description = "매장 유형", example = "ALL")
            @RequestParam(defaultValue = "ALL") StoreType type,

            @Parameter(description = "추천 개수", example = "3")
            @RequestParam(defaultValue = "3") int limit
    ) {
        List<StoreRecommendationResponse> stores = storeRecommendationService.recommendStores(
                getUserId(jwt),
                keyword,
                lat,
                lng,
                type,
                limit
        );
        return ApiResponse.success(SuccessStatus.FETCHED, stores);
    }

    @Operation(
            summary = "매장 상세 조회",
            description = """
                    매장 상세 정보와 등록된 상품/재고/태그 목록을 조회합니다.

                    - lat, lng를 함께 전달하면 현재 위치 기준 거리를 계산합니다.
                    - lat, lng를 생략하면 distance는 0으로 반환됩니다.
                    
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

    private Long getUserId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
