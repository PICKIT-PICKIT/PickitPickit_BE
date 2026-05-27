package PickitPickit.user.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_users_kakao_id",
                        columnNames = "kakao_id"
                ),
                @UniqueConstraint(
                        name = "uq_users_nickname",
                        columnNames = "nickname"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, length = 100)
    private String kakaoId;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "kakao_profile_image_url", length = 500)
    private String kakaoProfileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_image_type", length = 20)
    private ProfileImageType profileImageType;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Builder
    private User(String kakaoId, String nickname, String profileImageUrl,
                 String kakaoProfileImageUrl, ProfileImageType profileImageType,
                 boolean onboardingCompleted, UserRole role) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.kakaoProfileImageUrl = kakaoProfileImageUrl;
        this.profileImageType = profileImageType;
        this.onboardingCompleted = onboardingCompleted;
        this.role = role == null ? UserRole.USER : role;
    }

    public static User createFromKakao(String kakaoId, String nickname, String profileImageUrl) {
        ProfileImageType profileImageType = profileImageUrl != null && !profileImageUrl.isBlank()
                ? ProfileImageType.KAKAO
                : null;

        return User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .kakaoProfileImageUrl(profileImageUrl)
                .profileImageType(profileImageType)
                .onboardingCompleted(false)
                .role(UserRole.USER)
                .build();
    }

    public void updateKakaoProfileImageUrl(String kakaoProfileImageUrl) {
        this.kakaoProfileImageUrl = kakaoProfileImageUrl;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.onboardingCompleted = false;
    }

    public void updateProfileImage(ProfileImageType profileImageType, String profileImageUrl) {
        this.profileImageType = profileImageType;
        this.profileImageUrl = profileImageUrl;
        this.onboardingCompleted = false;
    }

    public void markOnboardingIncomplete() {
        this.onboardingCompleted = false;
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void changeRole(UserRole role) {
        this.role = role == null ? UserRole.USER : role;
    }

    // 마이페이지 수정용
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImage(ProfileImageType profileImageType, String profileImageUrl) {
        this.profileImageType = profileImageType;
        this.profileImageUrl = profileImageUrl;
    }
}
