package PickitPickit.auth.service;

import PickitPickit.auth.repository.RefreshTokenRepository;
import PickitPickit.global.security.TokenHashEncoder;
import PickitPickit.user.domain.User;
import PickitPickit.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenServiceIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenHashEncoder tokenHashEncoder;

    @Test
    void rotateReplacesExistingRefreshTokenForSameUser() {
        User user = userRepository.save(User.createFromKakao("12345", "민수", null));
        Instant expiresAt = Instant.now().plusSeconds(3600);

        refreshTokenService.rotate(user, "first-refresh-token", expiresAt);
        refreshTokenService.rotate(user, "second-refresh-token", expiresAt);

        assertThat(refreshTokenRepository.findAll()).hasSize(1);
        assertThat(refreshTokenRepository.findByTokenHash(tokenHashEncoder.hash("first-refresh-token"))).isEmpty();
        assertThat(refreshTokenRepository.findByTokenHash(tokenHashEncoder.hash("second-refresh-token"))).isPresent();
    }
}
