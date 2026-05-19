package PickitPickit.global.security;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("refreshTokenJwtDecoder") JwtDecoder jwtDecoder,
            JwtProperties jwtProperties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

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

    public Long getRefreshTokenUserId(String refreshToken) {
        Jwt jwt = decodeRefreshToken(refreshToken);

        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorStatus.REFRESH_TOKEN_INVALID, "리프레시 토큰의 사용자 정보가 올바르지 않습니다.");
        }
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

    private Jwt decodeRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            validateRefreshTokenType(jwt);
            return jwt;
        } catch (JwtValidationException e) {
            if (e.getMessage() != null && e.getMessage().contains("expired")) {
                throw new ApiException(ErrorStatus.REFRESH_TOKEN_EXPIRED, "만료된 리프레시 토큰입니다.");
            }
            throw new ApiException(ErrorStatus.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorStatus.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private void validateRefreshTokenType(Jwt jwt) {
        if (!REFRESH_TOKEN_TYPE.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))) {
            throw new ApiException(ErrorStatus.REFRESH_TOKEN_INVALID, "리프레시 토큰이 아닙니다.");
        }
    }
}
