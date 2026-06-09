package PickitPickit.store.controller;

import PickitPickit.global.response.ApiResponse;
import PickitPickit.global.response.SuccessStatus;
import PickitPickit.store.dto.ItemResponse;
import PickitPickit.store.service.ItemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(
            summary = "매장 상품 등록용 상품 마스터 조회(itemId 확인)",
            description = """
                    owner가 자기 매장에 상품을 등록하기 전에 사용할 itemId와 상품 마스터 기본 태그를 조회합니다.
                    검색 결과의 itemId를 /api/owner/store-products 요청 본문에 넣어 매장 상품으로 연결합니다.
                    검색 결과가 없으면 /api/admin/items에서 상품 마스터를 먼저 생성한 뒤 생성 응답의 itemId를 사용합니다.
                    """
    )
    @GetMapping
    @PreAuthorize("@rolePermissionChecker.isAdminOrStoreOwner(authentication)")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItems(
            @Parameter(description = "상품명 검색어. 생략하면 전체 상품 마스터를 이름순으로 조회합니다.", example = "손오공")
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(SuccessStatus.FETCHED, itemAdminService.getItems(keyword));
    }
}
