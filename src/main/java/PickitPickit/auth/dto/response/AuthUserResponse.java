package PickitPickit.auth.dto.response;

import PickitPickit.user.domain.User;

public record AuthUserResponse(
        Long id,
        String nickname,
        String profileImageUrl
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }
}
