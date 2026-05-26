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

@Tag(name = "Owner - Store Product", description = "매장주용 상품/재고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/store-products")
@SecurityRequirement(name = "bearerAuth")
public class StoreProductOwnerController {

    private final StoreProductAdminService storeProductAdminService;

    @Operation(summary = "내 매장 상품 등록", description = "매장주는 본인이 연결된 매장에만 상품을 등록할 수 있습니다.")
    @PostMapping
    @PreAuthorize("@storePermissionChecker.canManageStore(authentication, #request.storeId())")
    public ResponseEntity<ApiResponse<StoreProductResponse>> create(
            @Valid @RequestBody StoreProductCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, storeProductAdminService.create(request));
    }

    @Operation(summary = "내 매장 상품 수정")
    @PatchMapping("/{storeProductId}")
    @PreAuthorize("@storePermissionChecker.canManageStoreProduct(authentication, #storeProductId)")
    public ResponseEntity<ApiResponse<StoreProductResponse>> update(
            @PathVariable Long storeProductId,
            @Valid @RequestBody StoreProductUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, storeProductAdminService.update(storeProductId, request));
    }

    @Operation(summary = "내 매장 상품 삭제")
    @DeleteMapping("/{storeProductId}")
    @PreAuthorize("@storePermissionChecker.canManageStoreProduct(authentication, #storeProductId)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long storeProductId
    ) {
        storeProductAdminService.delete(storeProductId);
        return ApiResponse.success(SuccessStatus.DELETED);
    }
}
