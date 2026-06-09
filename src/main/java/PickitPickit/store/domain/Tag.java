package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Getter
@Entity
@Table(
        name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tags_normalized_name", columnNames = "normalized_name")
        },
        indexes = {
                @Index(name = "idx_tags_name", columnList = "name")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseTimeEntity {

    private static final int MAX_NAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = MAX_NAME_LENGTH)
    private String normalizedName;

    @Builder
    private Tag(String name) {
        this.name = normalizeDisplayName(name);
        this.normalizedName = normalizeForLookup(name);
    }

    public static Tag create(String name) {
        return Tag.builder()
                .name(name)
                .build();
    }

    public static String normalizeForLookup(String name) {
        String normalized = normalizeDisplayName(name).toLowerCase(Locale.ROOT);
        return normalized.replaceAll("\\s+", " ");
    }

    private static String normalizeDisplayName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "태그명은 필수입니다.");
        }

        String normalized = name.trim().replaceAll("\\s+", " ");
        if (normalized.length() > MAX_NAME_LENGTH) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "태그명은 50자 이하여야 합니다.");
        }
        return normalized;
    }
}
