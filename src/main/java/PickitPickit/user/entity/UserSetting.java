package PickitPickit.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_settings")
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistanceUnit distanceUnit;

    @Column(nullable = false)
    private boolean pushAlarmEnabled;

    /**
     * 연관관계 주인 (FK 보유)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    /* =========================
       기본 설정 생성
       ========================= */

    public static UserSetting createDefault() {
        return UserSetting.builder()
                .distanceUnit(DistanceUnit.KM)
                .pushAlarmEnabled(true)
                .build();
    }

    /* =========================
       연관관계 설정
       ========================= */

    public void setUser(User user) {
        this.user = user;
    }

    /* =========================
       설정 변경
       ========================= */

    public void updateSetting(
            DistanceUnit distanceUnit,
            Boolean pushAlarmEnabled
    ) {

        if (distanceUnit != null) {
            this.distanceUnit = distanceUnit;
        }

        if (pushAlarmEnabled != null) {
            this.pushAlarmEnabled = pushAlarmEnabled;
        }
    }
}