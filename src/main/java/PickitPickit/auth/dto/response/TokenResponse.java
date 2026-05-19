package PickitPickit.auth.dto.response;

import PickitPickit.global.security.TokenPair;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {

    public static TokenResponse from(TokenPair tokenPair) {
        return new TokenResponse(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
