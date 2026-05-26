package PickitPickit.global.security;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String ROLE_CLAIM = "role";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder accessTokenJwtDecoder;
    private final JwtDecoder refreshTokenJwtDecoder;

    @Value("${jwt.access-token-validity-seconds:3600}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds:1209600}")
    private long refreshTokenValiditySeconds;

    @Value("${jwt.issuer:pickitpickit}")
    private String issuer;

    public JwtTokenProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("jwtDecoder") JwtDecoder accessTokenJwtDecoder,
            @Qualifier("refreshTokenJwtDecoder") JwtDecoder refreshTokenJwtDecoder
    ) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenJwtDecoder = accessTokenJwtDecoder;
        this.refreshTokenJwtDecoder = refreshTokenJwtDecoder;
    }

    public TokenPair createTokenPair(User user) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusSeconds(accessTokenValiditySeconds);
        Instant refreshExpiresAt = now.plusSeconds(refreshTokenValiditySeconds);

        String accessToken = createToken(user, now, accessExpiresAt, ACCESS_TOKEN_TYPE);
        String refreshToken = createToken(user, now, refreshExpiresAt, REFRESH_TOKEN_TYPE);

        return new TokenPair(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
    }

    public Long getRefreshTokenUserId(String refreshToken) {
        Jwt jwt = decode(refreshTokenJwtDecoder, refreshToken, ErrorStatus.REFRESH_TOKEN_INVALID);
        validateTokenType(jwt, REFRESH_TOKEN_TYPE, ErrorStatus.REFRESH_TOKEN_INVALID);
        return parseSubject(jwt.getSubject(), ErrorStatus.REFRESH_TOKEN_INVALID);
    }

    public Long getAccessTokenUserId(String accessToken) {
        Jwt jwt = decode(accessTokenJwtDecoder, accessToken, ErrorStatus.INVALID_INPUT);
        validateTokenType(jwt, ACCESS_TOKEN_TYPE, ErrorStatus.INVALID_INPUT);
        return parseSubject(jwt.getSubject(), ErrorStatus.INVALID_INPUT);
    }

    private String createToken(User user, Instant issuedAt, Instant expiresAt, String tokenType) {
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(String.valueOf(user.getId()))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim(ROLE_CLAIM, user.getRole().name())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }

    private Jwt decode(JwtDecoder jwtDecoder, String token, ErrorStatus errorStatus) {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            throw new ApiException(errorStatus, "유효하지 않은 토큰입니다.");
        }
    }

    private void validateTokenType(Jwt jwt, String expectedTokenType, ErrorStatus errorStatus) {
        String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
        if (!expectedTokenType.equals(tokenType)) {
            throw new ApiException(errorStatus, "토큰 종류가 올바르지 않습니다.");
        }
    }

    private Long parseSubject(String subject, ErrorStatus errorStatus) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new ApiException(errorStatus, "토큰 subject가 올바르지 않습니다.");
        }
    }
}