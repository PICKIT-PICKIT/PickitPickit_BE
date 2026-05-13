package PickitPickit.auth.dto.response;

import PickitPickit.global.security.TokenPair;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        AuthUserResponse user
) {

    public static LoginResponse of(TokenPair tokenPair, AuthUserResponse user) {
        return new LoginResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                user
        );
    }
}
