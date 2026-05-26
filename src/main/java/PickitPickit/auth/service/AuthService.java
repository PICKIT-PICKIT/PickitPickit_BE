package PickitPickit.auth.service;

import PickitPickit.auth.client.KakaoAuthClient;
import PickitPickit.auth.domain.RefreshToken;
import PickitPickit.auth.dto.client.KakaoUserResponse;
import PickitPickit.auth.dto.request.KakaoLoginRequest;
import PickitPickit.auth.dto.request.LogoutRequest;
import PickitPickit.auth.dto.request.RefreshTokenRequest;
import PickitPickit.auth.dto.response.AuthUserResponse;
import PickitPickit.auth.dto.response.LoginResponse;
import PickitPickit.auth.dto.response.TokenResponse;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.global.security.JwtTokenProvider;
import PickitPickit.global.security.TokenPair;
import PickitPickit.user.domain.User;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserResponse kakaoUser = kakaoAuthClient.getUserInfo(request.kakaoAccessToken());
        User user = findOrCreateUser(kakaoUser);
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user);

        refreshTokenService.rotate(user, tokenPair.refreshToken(), tokenPair.refreshTokenExpiresAt());

        return LoginResponse.of(tokenPair, AuthUserResponse.from(user));
    }

    @Transactional
    public TokenResponse reissue(RefreshTokenRequest request) {
        Long tokenUserId = jwtTokenProvider.getRefreshTokenUserId(request.refreshToken());
        RefreshToken savedToken = refreshTokenService.getValidToken(request.refreshToken());
        User user = savedToken.getUser();

        if (!user.getId().equals(tokenUserId)) {
            throw new ApiException(ErrorStatus.REFRESH_TOKEN_INVALID, "리프레시 토큰의 사용자 정보가 일치하지 않습니다.");
        }

        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user);
        refreshTokenService.rotate(user, tokenPair.refreshToken(), tokenPair.refreshTokenExpiresAt());

        return TokenResponse.from(tokenPair);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));

        return AuthUserResponse.from(user);
    }

    private User findOrCreateUser(KakaoUserResponse kakaoUser) {
        if (kakaoUser == null || kakaoUser.id() == null) {
            throw new ApiException(ErrorStatus.KAKAO_LOGIN_FAILED, "카카오 회원번호를 조회할 수 없습니다.");
        }

        String kakaoId = String.valueOf(kakaoUser.id());
        String profileImageUrl = kakaoUser.profileImageUrl();

        return userRepository.findByKakaoId(kakaoId)
                .map(user -> updateKakaoProfileImage(user, profileImageUrl))
                .orElseGet(() -> userRepository.save(User.createFromKakao(
                        kakaoId,
                        createInitialNickname(kakaoUser.nickname(), kakaoId),
                        profileImageUrl
                )));
    }

    private User updateKakaoProfileImage(User user, String profileImageUrl) {
        user.updateKakaoProfileImageUrl(profileImageUrl);
        return user;
    }

    private String createInitialNickname(String preferredNickname, String kakaoId) {
        if (StringUtils.hasText(preferredNickname) && !userRepository.existsByNickname(preferredNickname)) {
            return preferredNickname;
        }

        String temporaryNickname = "pickit_" + kakaoId;
        if (!userRepository.existsByNickname(temporaryNickname)) {
            return temporaryNickname;
        }

        throw new ApiException(ErrorStatus.DUPLICATE_NICKNAME, "초기 닉네임을 생성할 수 없습니다.");
    }
}
