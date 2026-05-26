package PickitPickit.store.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import PickitPickit.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "store_managers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_store_manager", columnNames = {"store_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_store_managers_store_id", columnList = "store_id"),
                @Index(name = "idx_store_managers_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreManager extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreManagerRole role;

    @Builder
    private StoreManager(Store store, User user, StoreManagerRole role) {
        this.store = store;
        this.user = user;
        this.role = role == null ? StoreManagerRole.OWNER : role;
    }

    public static StoreManager create(Store store, User user, StoreManagerRole role) {
        return StoreManager.builder()
                .store(store)
                .user(user)
                .role(role)
                .build();
    }
}
