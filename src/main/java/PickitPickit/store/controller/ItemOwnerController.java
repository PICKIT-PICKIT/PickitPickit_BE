package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.ItemResponse;
import PickitPickit.store.service.ItemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Owner - Item", description = "매장주용 상품 마스터 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner/items")
@SecurityRequirement(name = "bearerAuth")
public class ItemOwnerController {

    private final ItemAdminService itemAdminService;

    @Operation(summary = "상품 마스터 목록 조회", description = "매장 상품 등록에 사용할 itemId와 기본 태그를 조회합니다.")
    @GetMapping
    @PreAuthorize("@rolePermissionChecker.isAdminOrStoreOwner(authentication)")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, itemAdminService.getItems(keyword));
    }
}
