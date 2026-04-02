package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    @Column(name = "default_image_url", length = 500)
    private String defaultImageUrl;

    @Builder
    private Item(String name, ProductCategory category, String defaultImageUrl) {
        this.name = name;
        this.category = category;
        this.defaultImageUrl = defaultImageUrl;
    }

    public static Item create(String name, ProductCategory category, String defaultImageUrl) {
        return Item.builder()
                .name(name)
                .category(category)
                .defaultImageUrl(defaultImageUrl)
                .build();
    }
}
