package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.StoreTagReplaceRequest;
import PickitPickit.store.dto.TagResponse;
import PickitPickit.store.service.StoreTagAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin - Store Tag", description = "관리자용 매장 대표 태그 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stores")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("@rolePermissionChecker.isAdmin(authentication)")
public class StoreTagAdminController {

    private final StoreTagAdminService storeTagAdminService;

    @Operation(summary = "매장 대표 태그 조회")
    @GetMapping("/{storeId}/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getStoreTags(
            @PathVariable Long storeId
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, storeTagAdminService.getStoreTags(storeId));
    }

    @Operation(summary = "매장 대표 태그 교체", description = "수동 태그만 교체합니다. 상품 태그에서 자동 반영된 태그는 상품이 남아 있으면 유지됩니다.")
    @PutMapping("/{storeId}/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> replaceStoreTags(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreTagReplaceRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, storeTagAdminService.replaceStoreTags(storeId, request));
    }
}
