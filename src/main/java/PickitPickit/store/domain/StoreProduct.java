package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "store_products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_products", columnNames = {"store_id", "item_id"})
        },
        indexes = {
                @Index(name = "idx_store_products_store_id", columnList = "store_id"),
                @Index(name = "idx_store_products_item_id", columnList = "item_id"),
                @Index(name = "idx_store_products_stock_status", columnList = "stock_status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreProduct extends BaseTimeEntity {

    private static final int LOW_STOCK_THRESHOLD = 2;
    private static final int MAX_URL_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "price")
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_mode", nullable = false, length = 20)
    private InventoryMode inventoryMode;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", nullable = false, length = 20)
    private StockStatus stockStatus;

    @Column(name = "difficulty")
    private Integer difficulty; // 1~5, 상품/기계 기준 난이도

    @Column(name = "image_url", length = MAX_URL_LENGTH)
    private String imageUrl;

    @Builder
    private StoreProduct(Store store, Item item, Integer price,
                         InventoryMode inventoryMode, Integer stockQuantity,
                         StockStatus stockStatus, Integer difficulty, String imageUrl) {
        this.store = validateStore(store);
        this.item = validateItem(item);
        this.price = validatePrice(price);
        this.inventoryMode = validateInventoryMode(inventoryMode);
        this.stockQuantity = validateStockQuantity(stockQuantity);
        this.stockStatus = resolveStockStatus(this.inventoryMode, this.stockQuantity, stockStatus);
        this.difficulty = validateDifficulty(difficulty);
        this.imageUrl = normalizeNullableUrl(imageUrl);
    }

    public static StoreProduct create(Store store, Item item, Integer price,
                                      InventoryMode inventoryMode, Integer stockQuantity,
                                      StockStatus stockStatus, Integer difficulty, String imageUrl) {
        return StoreProduct.builder()
                .store(store)
                .item(item)
                .price(price)
                .inventoryMode(inventoryMode)
                .stockQuantity(stockQuantity)
                .stockStatus(stockStatus)
                .difficulty(difficulty)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(Integer price, InventoryMode inventoryMode, Integer stockQuantity,
                       StockStatus stockStatus, Integer difficulty, String imageUrl) {
        this.price = validatePrice(price);
        this.inventoryMode = validateInventoryMode(inventoryMode);
        this.stockQuantity = validateStockQuantity(stockQuantity);
        this.stockStatus = resolveStockStatus(this.inventoryMode, this.stockQuantity, stockStatus);
        this.difficulty = validateDifficulty(difficulty);
        this.imageUrl = normalizeNullableUrl(imageUrl);
    }

    public String getEffectiveImageUrl() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }
        return item != null ? item.getDefaultImageUrl() : null;
    }

    public String getDifficultyLabel() {
        if (difficulty == null) {
            return null;
        }
        return switch (difficulty) {
            case 1 -> "매우 쉬움";
            case 2 -> "쉬움";
            case 3 -> "보통";
            case 4 -> "어려움";
            case 5 -> "매우 어려움";
            default -> null;
        };
    }

    private static Store validateStore(Store store) {
        if (store == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "매장은 필수입니다.");
        }
        return store;
    }

    private static Item validateItem(Item item) {
        if (item == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "상품은 필수입니다.");
        }
        return item;
    }

    private static Integer validatePrice(Integer price) {
        if (price != null && price < 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "가격은 0 이상이어야 합니다.");
        }
        return price;
    }

    private static InventoryMode validateInventoryMode(InventoryMode inventoryMode) {
        if (inventoryMode == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "재고 관리 방식은 필수입니다.");
        }
        return inventoryMode;
    }

    private static Integer validateStockQuantity(Integer stockQuantity) {
        if (stockQuantity != null && stockQuantity < 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "재고 수량은 0 이상이어야 합니다.");
        }
        return stockQuantity;
    }

    private static StockStatus resolveStockStatus(InventoryMode inventoryMode, Integer stockQuantity, StockStatus stockStatus) {
        if (inventoryMode == InventoryMode.QUANTITY) {
            if (stockQuantity == null) {
                throw new ApiException(ErrorStatus.INVALID_INPUT, "수량 관리 방식에서는 재고 수량이 필수입니다.");
            }
            if (stockQuantity == 0) {
                return StockStatus.OUT_OF_STOCK;
            }
            if (stockQuantity <= LOW_STOCK_THRESHOLD) {
                return StockStatus.LOW_STOCK;
            }
            return StockStatus.IN_STOCK;
        }

        if (stockStatus == null) {
            return StockStatus.UNKNOWN;
        }
        return stockStatus;
    }

    private static Integer validateDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return null;
        }
        if (difficulty < 1 || difficulty > 5) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "상품 난이도는 1 이상 5 이하만 입력할 수 있습니다.");
        }
        return difficulty;
    }

    private static String normalizeNullableUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_URL_LENGTH) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "이미지 URL은 500자 이하여야 합니다.");
        }
        return normalized;
    }
}
