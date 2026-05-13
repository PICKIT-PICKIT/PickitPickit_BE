package PickitPickit.auth.service;

import PickitPickit.auth.client.KakaoAuthClient;
import PickitPickit.auth.dto.client.KakaoUserResponse;
import PickitPickit.auth.dto.request.KakaoLoginRequest;
import PickitPickit.auth.dto.response.AuthUserResponse;
import PickitPickit.auth.dto.response.LoginResponse;
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
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user.getId());

        refreshTokenService.rotate(user, tokenPair.refreshToken(), tokenPair.refreshTokenExpiresAt());

        return LoginResponse.of(tokenPair, AuthUserResponse.from(user));
    }

    private User findOrCreateUser(KakaoUserResponse kakaoUser) {
        if (kakaoUser == null || kakaoUser.id() == null) {
            throw new ApiException(ErrorStatus.KAKAO_LOGIN_FAILED, "카카오 회원번호를 조회할 수 없습니다.");
        }

        String nickname = kakaoUser.nickname();
        if (!StringUtils.hasText(nickname)) {
            throw new ApiException(ErrorStatus.KAKAO_LOGIN_FAILED, "카카오 닉네임 동의가 필요합니다.");
        }

        String kakaoId = String.valueOf(kakaoUser.id());
        String profileImageUrl = kakaoUser.profileImageUrl();

        return userRepository.findByKakaoId(kakaoId)
                .map(user -> updateUserProfile(user, nickname, profileImageUrl))
                .orElseGet(() -> userRepository.save(User.createFromKakao(kakaoId, nickname, profileImageUrl)));
    }

    private User updateUserProfile(User user, String nickname, String profileImageUrl) {
        user.updateProfile(nickname, profileImageUrl);
        return user;
    }
}
