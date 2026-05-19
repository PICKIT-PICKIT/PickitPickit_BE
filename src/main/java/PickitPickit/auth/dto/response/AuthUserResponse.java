package PickitPickit.auth.dto.response;

import PickitPickit.user.domain.User;

public record AuthUserResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        boolean onboardingCompleted
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.isOnboardingCompleted()
        );
    }
}
