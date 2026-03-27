package PickitPickit.review.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_store",
                        columnNames = {"user_id", "store_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

    private static final BigDecimal MIN_RATING = new BigDecimal("0.5");
    private static final BigDecimal MAX_RATING = new BigDecimal("5.0");
    private static final BigDecimal STEP = new BigDecimal("0.5");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private double rating;

    @Column(name = "difficulty")
    private Integer difficulty; // 1~5, 선택값

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    private Review(Long userId, Long storeId, double rating, Integer difficulty, String content, String imageUrl) {
        validateRating(rating);
        validateDifficulty(difficulty);
        this.userId = userId;
        this.storeId = storeId;
        this.rating = rating;
        this.difficulty = difficulty;
        this.content = normalize(content);
        this.imageUrl = normalize(imageUrl);
    }

    public static Review create(Long userId, Long storeId, double rating, Integer difficulty, String content, String imageUrl) {
        return Review.builder()
                .userId(userId)
                .storeId(storeId)
                .rating(rating)
                .difficulty(difficulty)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(double rating, Integer difficulty, String content, String imageUrl) {
        validateRating(rating);
        validateDifficulty(difficulty);
        this.rating = rating;
        this.difficulty = difficulty;
        this.content = normalize(content);
        this.imageUrl = normalize(imageUrl);
    }

    private static void validateRating(double rating) {
        BigDecimal value = BigDecimal.valueOf(rating);

        if (value.compareTo(MIN_RATING) < 0 || value.compareTo(MAX_RATING) > 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "별점은 0.5 이상 5.0 이하만 입력할 수 있습니다.");
        }

        if (value.remainder(STEP).compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "별점은 0.5 단위로만 입력할 수 있습니다.");
        }
    }

    private static void validateDifficulty(Integer difficulty) {
        if (difficulty == null) {
            return;
        }

        if (difficulty < 1 || difficulty > 5) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "난이도는 1 이상 5 이하만 입력할 수 있습니다.");
        }
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}