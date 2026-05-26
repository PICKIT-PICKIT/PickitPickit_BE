package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.StoreManagerCreateRequest;
import PickitPickit.store.dto.StoreManagerResponse;
import PickitPickit.store.service.StoreManagerAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Store Manager", description = "매장주/매장 직원 지정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/store-managers")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
public class StoreManagerAdminController {

    private final StoreManagerAdminService storeManagerAdminService;

    @Operation(summary = "매장주/직원 지정", description = "카카오 로그인으로 가입한 사용자를 특정 매장의 관리자로 연결합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<StoreManagerResponse>> createStoreManager(
            @Valid @RequestBody StoreManagerCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, storeManagerAdminService.createStoreManager(request));
    }
}
