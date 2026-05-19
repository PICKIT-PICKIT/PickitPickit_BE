package PickitPickit.auth.service;

import PickitPickit.auth.domain.RefreshToken;
import PickitPickit.auth.repository.RefreshTokenRepository;
import PickitPickit.global.security.TokenHashEncoder;
import PickitPickit.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenHashEncoder tokenHashEncoder;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, tokenHashEncoder);
    }

    @Test
    void rotateStoresHashedRefreshTokenOnly() {
        User user = User.createFromKakao("12345", "민수", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        Instant expiresAt = Instant.parse("2026-05-13T00:00:00Z");

        when(tokenHashEncoder.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");

        refreshTokenService.rotate(user, "raw-refresh-token", expiresAt);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).deleteByUserId(1L);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getTokenHash()).isEqualTo("hashed-refresh-token");
        assertThat(savedToken.getTokenHash()).isNotEqualTo("raw-refresh-token");
    }

    @Test
    void getValidTokenReturnsStoredTokenWhenTokenExistsAndNotExpired() {
        User user = User.createFromKakao("12345", "민수", null);
        RefreshToken refreshToken = RefreshToken.create(user, "hashed-refresh-token", LocalDateTime.now().plusDays(1));

        when(tokenHashEncoder.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");
        when(refreshTokenRepository.findByTokenHash("hashed-refresh-token")).thenReturn(Optional.of(refreshToken));

        assertThat(refreshTokenService.getValidToken("raw-refresh-token")).isEqualTo(refreshToken);
    }

    @Test
    void revokeDeletesTokenByHash() {
        when(tokenHashEncoder.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");

        refreshTokenService.revoke("raw-refresh-token");

        verify(refreshTokenRepository).deleteByTokenHash("hashed-refresh-token");
    }

    @Test
    void getValidTokenFailsWhenStoredTokenDoesNotExist() {
        when(tokenHashEncoder.hash("revoked-refresh-token")).thenReturn("hashed-revoked-refresh-token");
        when(refreshTokenRepository.findByTokenHash("hashed-revoked-refresh-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.getValidToken("revoked-refresh-token"))
                .hasMessage("서버에 등록되지 않은 리프레시 토큰입니다.");
    }
}
