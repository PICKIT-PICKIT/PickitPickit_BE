package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.ItemCreateRequest;
import PickitPickit.store.dto.ItemResponse;
import PickitPickit.store.dto.ItemUpdateRequest;
import PickitPickit.store.service.ItemAdminService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@Tag(name = "Admin - Item", description = "상품 마스터 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/items")
@SecurityRequirement(name = "bearerAuth")
public class ItemAdminController {

    private final ItemAdminService itemAdminService;

    @Operation(
            summary = "상품 마스터 등록",
            description = """
                    ADMIN 또는 STORE_OWNER가 호출할 수 있습니다.
                    owner 상품 등록 화면에서 /api/owner/items 검색 결과에 없는 상품을 먼저 상품 마스터로 생성할 때 사용합니다.
                    생성 응답의 itemId를 /api/owner/store-products 요청에 전달해 매장 상품으로 등록합니다.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ItemCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "상품 마스터 생성 예시",
                                    value = """
                                            {
                                              "name": "손오공 피규어",
                                              "category": "PLUSH",
                                              "defaultImageUrl": null,
                                              "tags": ["드래곤볼", "손오공"]
                                            }
                                            """
                            )
                    )
            )
    )
    @PostMapping
    @PreAuthorize("@rolePermissionChecker.isAdminOrStoreOwner(authentication)")
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody ItemCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, itemAdminService.create(request));
    }

    @Operation(summary = "상품 마스터 목록 조회")
    @GetMapping
    @PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems() {
        return ApiResponse.success(SuccessStatus.FETCHED, itemAdminService.getItems());
    }

    @Operation(summary = "상품 마스터 수정")
    @PatchMapping("/{itemId}")
    @PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, itemAdminService.update(itemId, request));
    }
}
