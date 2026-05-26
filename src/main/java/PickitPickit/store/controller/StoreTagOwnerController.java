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

@Tag(name = "Owner - Store Tag", description = "매장주용 대표 태그 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/stores")
@SecurityRequirement(name = "bearerAuth")
public class StoreTagOwnerController {

    private final StoreTagAdminService storeTagAdminService;

    @Operation(summary = "내 매장 대표 태그 조회")
    @GetMapping("/{storeId}/tags")
    @PreAuthorize("@storePermissionChecker.canManageStore(authentication, #storeId)")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getStoreTags(
            @PathVariable Long storeId
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, storeTagAdminService.getStoreTags(storeId));
    }

    @Operation(summary = "내 매장 대표 태그 교체")
    @PutMapping("/{storeId}/tags")
    @PreAuthorize("@storePermissionChecker.canManageStore(authentication, #storeId)")
    public ResponseEntity<ApiResponse<List<TagResponse>>> replaceStoreTags(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreTagReplaceRequest request
    ) {
        return ApiResponse.success(SuccessStatus.UPDATED, storeTagAdminService.replaceStoreTags(storeId, request));
    }
}
