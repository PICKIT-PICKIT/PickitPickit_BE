package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.ItemCreateRequest;
import PickitPickit.store.dto.ItemResponse;
import PickitPickit.store.dto.ItemUpdateRequest;
import PickitPickit.store.service.ItemAdminService;
import io.swagger.v3.oas.annotations.Operation;
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
@PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
public class ItemAdminController {

    private final ItemAdminService itemAdminService;

    @Operation(summary = "상품 마스터 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(
            @Valid @RequestBody ItemCreateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.CREATED, itemAdminService.create(request));
    }

    @Operation(summary = "상품 마스터 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems() {
        return ApiResponse.success(SuccessStatus.FETCHED, itemAdminService.getItems());
    }

    @Operation(summary = "상품 마스터 수정")
    @PatchMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, itemAdminService.update(itemId, request));
    }
}
