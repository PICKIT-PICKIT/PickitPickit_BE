package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.FavoriteStoreResponse;
import PickitPickit.store.service.FavoriteStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Favorite Store", description = "관심매장 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/favorite-stores")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteStoreController {

    private final FavoriteStoreService favoriteStoreService;

    @Operation(
            summary = "내 관심매장 목록 조회",
            description = """
                    로그인한 사용자의 관심매장 목록을 조회합니다.

                    - lat, lng를 함께 전달하면 현재 위치 기준 거리를 계산합니다.
                    - lat, lng를 생략하면 distance는 0으로 반환됩니다.
                    - 로그인이 필요합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteStoreResponse>>> getMyFavoriteStores(
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "사용자 위도", example = "37.5665")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "사용자 경도", example = "126.9780")
            @RequestParam(required = false) Double lng
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        List<FavoriteStoreResponse> response = favoriteStoreService.getMyFavoriteStores(
                userId,
                lat,
                lng
        );

        return ApiResponse.success(SuccessStatus.FETCHED, response);
    }

    @Operation(
            summary = "관심매장 추가",
            description = """
                    로그인한 사용자가 특정 매장을 관심매장으로 등록합니다.

                    - 이미 등록된 매장이면 실패합니다.
                    - 로그인이 필요합니다.
                    """
    )
    @PostMapping("/{storeId}")
    public ResponseEntity<ApiResponse<FavoriteStoreResponse>> addFavoriteStore(
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        FavoriteStoreResponse response = favoriteStoreService.addFavoriteStore(userId, storeId);

        return ApiResponse.success(SuccessStatus.CREATED, response);
    }

    @Operation(
            summary = "관심매장 삭제",
            description = """
                    로그인한 사용자가 특정 매장을 관심매장에서 삭제합니다.

                    - 로그인이 필요합니다.
                    """
    )
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> removeFavoriteStore(
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        favoriteStoreService.removeFavoriteStore(userId, storeId);

        return ApiResponse.success(SuccessStatus.DELETED);
    }
}