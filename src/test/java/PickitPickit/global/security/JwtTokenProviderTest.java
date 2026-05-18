package PickitPickit.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Qualifier("jwtDecoder")
    private JwtDecoder accessTokenJwtDecoder;

    @Autowired
    @Qualifier("refreshTokenJwtDecoder")
    private JwtDecoder refreshTokenJwtDecoder;

    @Test
    void accessTokenDecoderAcceptsOnlyAccessTokenForProtectedApis() {
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(1L);

        assertThat(accessTokenJwtDecoder.decode(tokenPair.accessToken()).getSubject()).isEqualTo("1");
        assertThatThrownBy(() -> accessTokenJwtDecoder.decode(tokenPair.refreshToken()))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void refreshTokenDecoderStillAllowsRefreshTokenForReissueFlow() {
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(1L);

        assertThat(refreshTokenJwtDecoder.decode(tokenPair.refreshToken()).getClaimAsString("token_type"))
                .isEqualTo("refresh");
    }

    @Test
    void tokenProviderCanReadUserIdFromRefreshTokenForReissueFlow() {
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(1L);

        assertThat(jwtTokenProvider.getRefreshTokenUserId(tokenPair.refreshToken())).isEqualTo(1L);
    }
}
