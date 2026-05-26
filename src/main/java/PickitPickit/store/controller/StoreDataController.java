package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.loader.PublicApiStoreLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Admin - Store Data", description = "매장 데이터 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stores")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
public class StoreDataController {

    private final PublicApiStoreLoader publicApiStoreLoader;

    @Operation(summary = "공공데이터 매장 일괄 적재")
    @PostMapping("/load-public-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loadPublicData() {
        int loaded = publicApiStoreLoader.loadAll();
        return ApiResponse.success(
                SuccessStatus.STORE_DATA_LOADED,
                Map.of("loadedCount", loaded)
        );
    }
}
