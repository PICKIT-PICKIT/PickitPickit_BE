package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
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
                @UniqueConstraint(
                        name = "uq_store_products",
                        columnNames = {"store_id", "item_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_mode", nullable = false, length = 20)
    private InventoryMode inventoryMode;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_status", length = 20)
    private StockStatus stockStatus;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Builder
    private StoreProduct(Store store, Item item, Integer price,
                         InventoryMode inventoryMode, Integer stockQuantity,
                         StockStatus stockStatus, String imageUrl) {
        this.store = store;
        this.item = item;
        this.price = price;
        this.inventoryMode = inventoryMode;
        this.stockQuantity = stockQuantity;
        this.stockStatus = stockStatus;
        this.imageUrl = imageUrl;
    }

    /**
     * 실제 표시할 이미지 URL (매장별 > 상품 기본 이미지 순서)
     */
    public String getEffectiveImageUrl() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }
        return item != null ? item.getDefaultImageUrl() : null;
    }
}
