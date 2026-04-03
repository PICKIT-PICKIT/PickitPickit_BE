package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.loader.PublicApiStoreLoader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Admin - Store Data", description = "매장 데이터 관리 API (관리자용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stores")
public class StoreDataController {

    private final PublicApiStoreLoader publicApiStoreLoader;

    @Operation(
            summary = "공공데이터 매장 일괄 적재",
            description = """
                    공공데이터 포털(청소년게임제공업)에서 영업중인 매장 데이터를 조회하여
                    DB에 일괄 적재합니다. 이미 적재된 매장은 자동 스킵됩니다.
                    ⚠️ 첫 실행 시 수 분이 소요될 수 있습니다.
                    """
    )
    @PostMapping("/load-public-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> loadPublicData() {
        int loaded = publicApiStoreLoader.loadAll();
        return ApiResponse.success(SuccessStatus.STORE_DATA_LOADED,
                Map.of("loadedCount", loaded));
    }
}
