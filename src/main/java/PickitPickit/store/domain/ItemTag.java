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
        name = "item_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_item_tags", columnNames = {"item_id", "tag_id"})
        },
        indexes = {
                @Index(name = "idx_item_tags_item_id", columnList = "item_id"),
                @Index(name = "idx_item_tags_tag_id", columnList = "tag_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Builder
    private ItemTag(Item item, Tag tag) {
        this.item = item;
        this.tag = tag;
    }

    public static ItemTag create(Item item, Tag tag) {
        return ItemTag.builder()
                .item(item)
                .tag(tag)
                .build();
    }
}
