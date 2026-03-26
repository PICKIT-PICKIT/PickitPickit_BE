package PickitPickit.review.domain;

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
@Table(name = "brags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "spent_cost", nullable = false)
    private int spentCost;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    private Brag(Long userId, Long storeId, int spentCost, String imageUrl, String content) {
        validateSpentCost(spentCost);
        validateImageUrl(imageUrl);
        this.userId = userId;
        this.storeId = storeId;
        this.spentCost = spentCost;
        this.imageUrl = imageUrl.trim();
        this.content = normalize(content);
    }

    public static Brag create(Long userId, Long storeId, int spentCost, String imageUrl, String content) {
        return Brag.builder()
                .userId(userId)
                .storeId(storeId)
                .spentCost(spentCost)
                .imageUrl(imageUrl)
                .content(content)
                .build();
    }

    public void update(Long storeId, int spentCost, String imageUrl, String content) {
        validateSpentCost(spentCost);
        validateImageUrl(imageUrl);
        this.storeId = storeId;
        this.spentCost = spentCost;
        this.imageUrl = imageUrl.trim();
        this.content = normalize(content);
    }

    private static void validateSpentCost(int spentCost) {
        if (spentCost < 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "사용 금액은 0 이상이어야 합니다.");
        }
    }

    private static void validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "자랑하기 이미지는 필수입니다.");
        }
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}