package PickitPickit.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenPair createTokenPair(Long userId) {
        Instant now = Instant.now();
        Instant accessTokenExpiresAt = now.plusMillis(jwtProperties.getAccessTokenExpiration());
        Instant refreshTokenExpiresAt = now.plusMillis(jwtProperties.getRefreshTokenExpiration());

        return new TokenPair(
                createToken(userId, ACCESS_TOKEN_TYPE, now, accessTokenExpiresAt),
                createToken(userId, REFRESH_TOKEN_TYPE, now, refreshTokenExpiresAt),
                refreshTokenExpiresAt
        );
    }

    private String createToken(Long userId, String tokenType, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(userId))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }
}
