package PickitPickit.global.security;

import java.time.Instant;

public record TokenPair(
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
}
