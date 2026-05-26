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
        name = "store_tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_tags", columnNames = {"store_id", "tag_id"})
        },
        indexes = {
                @Index(name = "idx_store_tags_store_id", columnList = "store_id"),
                @Index(name = "idx_store_tags_tag_id", columnList = "tag_id"),
                @Index(name = "idx_store_tags_source", columnList = "source")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private StoreTagSource source;

    @Builder
    private StoreTag(Store store, Tag tag, StoreTagSource source) {
        this.store = store;
        this.tag = tag;
        this.source = source == null ? StoreTagSource.AUTO : source;
    }

    public static StoreTag create(Store store, Tag tag, StoreTagSource source) {
        return StoreTag.builder()
                .store(store)
                .tag(tag)
                .source(source)
                .build();
    }

    public void markManual() {
        this.source = StoreTagSource.MANUAL;
    }

    public void markAuto() {
        this.source = StoreTagSource.AUTO;
    }
}
