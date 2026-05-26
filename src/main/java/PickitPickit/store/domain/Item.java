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
        name = "items",
        indexes = {
                @Index(name = "idx_items_name", columnList = "name"),
                @Index(name = "idx_items_category", columnList = "category")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_URL_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    @Column(name = "default_image_url", length = MAX_URL_LENGTH)
    private String defaultImageUrl;

    @Builder
    private Item(String name, ProductCategory category, String defaultImageUrl) {
        this.name = normalizeName(name);
        this.category = validateCategory(category);
        this.defaultImageUrl = normalizeNullable(defaultImageUrl);
    }

    public static Item create(String name, ProductCategory category, String defaultImageUrl) {
        return Item.builder()
                .name(name)
                .category(category)
                .defaultImageUrl(defaultImageUrl)
                .build();
    }

    public void update(String name, ProductCategory category, String defaultImageUrl) {
        this.name = normalizeName(name);
        this.category = validateCategory(category);
        this.defaultImageUrl = normalizeNullable(defaultImageUrl);
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "상품명은 필수입니다.");
        }
        String normalized = name.trim();
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "상품명은 100자 이하여야 합니다.");
        }
        return normalized;
    }

    private static ProductCategory validateCategory(ProductCategory category) {
        if (category == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "상품 카테고리는 필수입니다.");
        }
        return category;
    }

    private static String normalizeNullable(String value) {
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
