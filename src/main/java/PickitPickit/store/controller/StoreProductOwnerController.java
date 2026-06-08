package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.StoreProductCreateRequest;
import PickitPickit.store.dto.StoreProductResponse;
import PickitPickit.store.dto.StoreProductUpdateRequest;
import PickitPickit.store.service.StoreProductAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(
            summary = "내 매장 상품 등록",
            description = """
                    매장주는 store_managers에 본인이 연결된 매장에만 상품을 등록할 수 있습니다.
                    itemId는 /api/owner/items 조회 결과 또는 /api/admin/items 상품 마스터 생성 응답에서 받은 값을 사용합니다.
                    같은 storeId + itemId 조합은 중복 등록할 수 없으며, 이미 등록된 상품은 PATCH /api/owner/store-products/{storeProductId}로 수정합니다.
                    tags를 생략하거나 null로 보내면 상품 마스터 기본 태그를 매장 상품 태그로 복사합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StoreProductCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "매장 상품 등록 예시",
                                    value = """
                                            {
                                              "storeId": 368,
                                              "itemId": 5,
                                              "price": 0,
                                              "inventoryMode": "QUANTITY",
                                              "stockQuantity": 0,
                                              "stockStatus": "IN_STOCK",
                                              "difficulty": 1,
                                              "imageUrl": null,
                                              "tags": ["드래곤볼"]
                                            }
                                            """
                            )
                    )
            )
    )
    @PostMapping
    @PreAuthorize("@storePermissionChecker.canManageStore(authentication, #request.storeId())")
    public ResponseEntity<ApiResponse<StoreProductResponse>> create(
            @Valid @RequestBody StoreProductCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, storeProductAdminService.create(request));
    }

    @Operation(
            summary = "내 매장 상품 수정",
            description = """
                    본인이 관리할 수 있는 매장 상품의 가격, 재고, 난이도, 이미지, 태그를 수정합니다.
                    tags를 생략하거나 null로 보내면 기존 태그를 유지하고, []로 보내면 전체 삭제합니다.
                    배열을 보내면 기존 태그를 요청 태그로 교체합니다. 없는 태그는 자동 생성되며, 중복 태그는 normalize 후 제거됩니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StoreProductUpdateRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "태그 교체 예시",
                                            value = """
                                                    {
                                                      "price": 0,
                                                      "inventoryMode": "QUANTITY",
                                                      "stockQuantity": 0,
                                                      "stockStatus": "IN_STOCK",
                                                      "difficulty": 1,
                                                      "imageUrl": null,
                                                      "tags": ["드래곤볼"]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "태그 전체 삭제 예시",
                                            value = """
                                                    {
                                                      "price": 0,
                                                      "inventoryMode": "STATUS",
                                                      "stockQuantity": null,
                                                      "stockStatus": "IN_STOCK",
                                                      "difficulty": 1,
                                                      "imageUrl": null,
                                                      "tags": []
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @PatchMapping("/{storeProductId}")
    @PreAuthorize("@storePermissionChecker.canManageStoreProduct(authentication, #storeProductId)")
    public ResponseEntity<ApiResponse<StoreProductResponse>> update(
            @Parameter(description = "수정할 매장 상품 ID", example = "10")
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
