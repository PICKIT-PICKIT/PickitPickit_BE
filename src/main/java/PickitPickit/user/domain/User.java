package PickitPickit.user.domain;

import PickitPickit.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private static final String WITHDRAWN_DISPLAY_NICKNAME = "탈퇴한 사용자";

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Builder
    private User(String kakaoId, String nickname, String profileImageUrl,
                 String kakaoProfileImageUrl, ProfileImageType profileImageType,
                 boolean onboardingCompleted, UserRole role,
                 UserStatus status, LocalDateTime withdrawnAt) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.kakaoProfileImageUrl = kakaoProfileImageUrl;
        this.profileImageType = profileImageType;
        this.onboardingCompleted = onboardingCompleted;
        this.role = role == null ? UserRole.USER : role;
        this.status = status == null ? UserStatus.ACTIVE : status;
        this.withdrawnAt = withdrawnAt;
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
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void updateKakaoProfileImageUrl(String kakaoProfileImageUrl) {
        if (isWithdrawn()) {
            return;
        }

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

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeProfileImage(ProfileImageType profileImageType, String profileImageUrl) {
        this.profileImageType = profileImageType;
        this.profileImageUrl = profileImageUrl;
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

    public void withdraw() {
        if (isWithdrawn()) {
            return;
        }

        if (this.id == null) {
            throw new IllegalStateException("영속화되지 않은 사용자는 탈퇴 처리할 수 없습니다.");
        }

        this.kakaoId = "withdrawn_" + this.id;
        this.nickname = "withdrawn_user_" + this.id;
        this.profileImageUrl = null;
        this.kakaoProfileImageUrl = null;
        this.profileImageType = null;
        this.onboardingCompleted = false;
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isWithdrawn() {
        return this.status == UserStatus.WITHDRAWN;
    }

    public String getDisplayNickname() {
        return isWithdrawn() ? WITHDRAWN_DISPLAY_NICKNAME : this.nickname;
    }

    public String getDisplayProfileImageUrl() {
        return isWithdrawn() ? null : this.profileImageUrl;
    }
}