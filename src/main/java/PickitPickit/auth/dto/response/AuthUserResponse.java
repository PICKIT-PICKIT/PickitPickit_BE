package PickitPickit.auth.dto.response;

import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserRole;

public record AuthUserResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        boolean onboardingCompleted,
        UserRole role
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.isOnboardingCompleted(),
                user.getRole()
        );
    }
}
