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
        name = "store_product_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_product_tags", columnNames = {"store_product_id", "tag_id"})
        },
        indexes = {
                @Index(name = "idx_store_product_tags_product_id", columnList = "store_product_id"),
                @Index(name = "idx_store_product_tags_tag_id", columnList = "tag_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreProductTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    private StoreProductTag(StoreProduct storeProduct, Tag tag) {
        this.storeProduct = storeProduct;
        this.tag = tag;
    }

    public static StoreProductTag create(StoreProduct storeProduct, Tag tag) {
        return StoreProductTag.builder()
                .storeProduct(storeProduct)
                .tag(tag)
                .build();
    }
}
