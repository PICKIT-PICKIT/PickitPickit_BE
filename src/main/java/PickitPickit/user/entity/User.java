package PickitPickit.user.entity;

import PickitPickit.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    private String profileImageUrl;

    /**
     * User는 비주인(mappedBy)
     */
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            optional = false
    )
    private UserSetting setting;

    /* =========================
       생성 로직 (실무 핵심 ⭐)
       ========================= */

    public static User create(String nickname, String email) {

        User user = User.builder()
                .nickname(nickname)
                .email(email)
                .build();

        // 기본 설정 자동 생성
        UserSetting defaultSetting = UserSetting.createDefault();

        user.initSetting(defaultSetting);

        return user;
    }

    /* =========================
       연관관계 편의 메서드
       ========================= */

    public void initSetting(UserSetting setting) {
        this.setting = setting;
        setting.setUser(this);
    }

    /* =========================
       비즈니스 로직
       ========================= */

    public void updateProfile(String nickname, String profileImageUrl) {

        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }

        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}