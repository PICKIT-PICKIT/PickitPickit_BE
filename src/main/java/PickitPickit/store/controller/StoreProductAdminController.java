package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.StoreProductCreateRequest;
import PickitPickit.store.dto.StoreProductResponse;
import PickitPickit.store.dto.StoreProductUpdateRequest;
import PickitPickit.store.service.StoreProductAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Store Product", description = "관리자용 매장별 상품/재고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/store-products")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
public class StoreProductAdminController {

    private final StoreProductAdminService storeProductAdminService;

    @Operation(summary = "매장 상품 등록", description = "관리자는 모든 매장에 상품, 가격, 재고, 난이도, 상품 태그를 등록할 수 있습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<StoreProductResponse>> create(
            @Valid @RequestBody StoreProductCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, storeProductAdminService.create(request));
    }

    @Operation(summary = "매장 상품 수정", description = "tags=null이면 기존 태그 유지, []이면 전체 삭제입니다.")
    @PatchMapping("/{storeProductId}")
    public ResponseEntity<ApiResponse<StoreProductResponse>> update(
            @PathVariable Long storeProductId,
            @Valid @RequestBody StoreProductUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, storeProductAdminService.update(storeProductId, request));
    }

    @Operation(summary = "매장 상품 삭제")
    @DeleteMapping("/{storeProductId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long storeProductId
    ) {
        storeProductAdminService.delete(storeProductId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }
}
