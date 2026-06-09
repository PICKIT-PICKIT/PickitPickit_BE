package PickitPickit.store.dto;

import PickitPickit.store.domain.InventoryMode;
import PickitPickit.store.domain.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

public record StoreProductCreateRequest(
        @Schema(description = "상품을 등록할 매장 ID. owner는 본인이 store_managers에 연결된 매장만 사용할 수 있습니다.", example = "368")
        @NotNull(message = "storeId는 필수입니다.")
        Long storeId,

        @Schema(description = "/api/owner/items 조회 결과 또는 /api/admin/items 생성 응답의 itemId", example = "5")
        @NotNull(message = "itemId는 필수입니다.")
        Long itemId,

        @Schema(description = "매장별 상품 가격. 미확인 가격이면 null, 무료/0원 상품이면 0을 보냅니다.", example = "0")
        @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
        Integer price,

        @Schema(description = "재고 관리 방식. 사용 가능 값: QUANTITY, STATUS", example = "QUANTITY")
        @NotNull(message = "inventoryMode는 필수입니다.")
        InventoryMode inventoryMode,

        @Schema(description = "수량 관리(QUANTITY)일 때 필수인 재고 수량. 0이면 OUT_OF_STOCK으로 저장됩니다.", example = "0")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다.")
        Integer stockQuantity,

        @Schema(description = "상태 관리(STATUS)일 때 사용할 재고 상태. 사용 가능 값: IN_STOCK, LOW_STOCK, OUT_OF_STOCK, UNKNOWN", example = "IN_STOCK")
        StockStatus stockStatus,

        @Schema(description = "매장 상품 난이도. 1~5 사이 값입니다.", example = "1")
        @Min(value = 1, message = "상품 난이도는 1 이상이어야 합니다.")
        @Max(value = 5, message = "상품 난이도는 5 이하여야 합니다.")
        Integer difficulty,

        @Schema(description = "매장별 상품 이미지 URL. null 또는 빈 문자열이면 상품 마스터 기본 이미지를 사용합니다.", example = "https://example.com/store-products/songoku.png")
        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl,

        @Schema(description = "매장 상품 태그명 목록. 생략/null이면 상품 마스터 기본 태그를 복사하고, []이면 태그 없이 등록합니다.", example = "[\"드래곤볼\"]")
        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
