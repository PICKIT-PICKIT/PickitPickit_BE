package PickitPickit.auth.service;

import PickitPickit.auth.domain.RefreshToken;
import PickitPickit.auth.repository.RefreshTokenRepository;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.global.security.TokenHashEncoder;
import PickitPickit.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashEncoder tokenHashEncoder;

    @Transactional
    public void rotate(User user, String refreshToken, Instant refreshTokenExpiresAt) {
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.create(
                user,
                tokenHashEncoder.hash(refreshToken),
                LocalDateTime.ofInstant(refreshTokenExpiresAt, ZoneId.systemDefault())
        ));
    }

    @Transactional(readOnly = true)
    public RefreshToken getValidToken(String refreshToken) {
        RefreshToken savedToken = refreshTokenRepository.findByTokenHash(tokenHashEncoder.hash(refreshToken))
                .orElseThrow(() -> new ApiException(
                        ErrorStatus.REFRESH_TOKEN_INVALID,
                        "서버에 등록되지 않은 리프레시 토큰입니다."
                ));

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorStatus.REFRESH_TOKEN_EXPIRED, "만료된 리프레시 토큰입니다.");
        }

        return savedToken;
    }

    @Transactional
    public void revoke(String refreshToken) {
        refreshTokenRepository.deleteByTokenHash(tokenHashEncoder.hash(refreshToken));
    }
}
