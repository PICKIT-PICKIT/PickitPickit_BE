package PickitPickit.auth.service;

import PickitPickit.auth.domain.RefreshToken;
import PickitPickit.auth.repository.RefreshTokenRepository;
import PickitPickit.global.security.TokenHashEncoder;
import PickitPickit.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashEncoder tokenHashEncoder;

    public void rotate(User user, String refreshToken, Instant refreshTokenExpiresAt) {
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.create(
                user,
                tokenHashEncoder.hash(refreshToken),
                LocalDateTime.ofInstant(refreshTokenExpiresAt, ZoneId.systemDefault())
        ));
    }
}
