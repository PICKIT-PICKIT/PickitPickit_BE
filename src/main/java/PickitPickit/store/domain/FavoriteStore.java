package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "favorite_stores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_favorite_stores_user_store",
                        columnNames = {"user_id", "store_id"}
                )
        },
        indexes = {
                @Index(name = "idx_favorite_stores_user_id", columnList = "user_id"),
                @Index(name = "idx_favorite_stores_store_id", columnList = "store_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteStore extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_stores_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "store_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_favorite_stores_store")
    )
    private Store store;

    private FavoriteStore(User user, Store store) {
        this.user = user;
        this.store = store;
    }

    public static FavoriteStore create(User user, Store store) {
        return new FavoriteStore(user, store);
    }
}