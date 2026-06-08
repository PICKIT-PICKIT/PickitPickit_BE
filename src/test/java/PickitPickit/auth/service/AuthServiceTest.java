package PickitPickit.auth.service;

import PickitPickit.auth.client.KakaoAuthClient;
import PickitPickit.auth.domain.RefreshToken;
import PickitPickit.auth.dto.client.KakaoUserResponse;
import PickitPickit.auth.dto.request.KakaoLoginRequest;
import PickitPickit.auth.dto.request.LogoutRequest;
import PickitPickit.auth.dto.request.RefreshTokenRequest;
import PickitPickit.auth.dto.response.LoginResponse;
import PickitPickit.auth.dto.response.TokenResponse;
import PickitPickit.global.security.JwtTokenProvider;
import PickitPickit.global.security.TokenPair;
import PickitPickit.user.domain.ProfileImageType;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String KAKAO_ACCESS_TOKEN = "kakao-access-token";

    @Mock
    private KakaoAuthClient kakaoAuthClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                kakaoAuthClient,
                userRepository,
                jwtTokenProvider,
                refreshTokenService
        );
    }

    @Test
    void loginWithKakaoCreatesNewUserWhenKakaoIdDoesNotExist() {
        KakaoUserResponse kakaoUser = kakaoUser(12345L, "민수", null);
        TokenPair tokenPair = tokenPair();

        when(kakaoAuthClient.getUserInfo(KAKAO_ACCESS_TOKEN)).thenReturn(kakaoUser);
        when(userRepository.findByKakaoIdAndStatus("12345", UserStatus.ACTIVE)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("민수")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });
        when(jwtTokenProvider.createTokenPair(any(User.class))).thenReturn(tokenPair);

        LoginResponse response = authService.loginWithKakao(new KakaoLoginRequest(KAKAO_ACCESS_TOKEN));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().nickname()).isEqualTo("민수");
        assertThat(response.user().profileImageUrl()).isNull();
        assertThat(response.user().onboardingCompleted()).isFalse();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getKakaoId()).isEqualTo("12345");
        assertThat(userCaptor.getValue().getProfileImageUrl()).isNull();
        verify(refreshTokenService).rotate(userCaptor.getValue(), "refresh-token", tokenPair.refreshTokenExpiresAt());
    }

    @Test
    void loginWithKakaoReusesExistingUserWithoutOverwritingOnboardingProfile() {
        User existingUser = User.createFromKakao("12345", "예전닉네임", "old-image");
        ReflectionTestUtils.setField(existingUser, "id", 1L);
        existingUser.updateNickname("커스텀닉네임");
        existingUser.updateProfileImage(ProfileImageType.DEFAULT, "/images/profile-defaults/default-1.png");

        when(kakaoAuthClient.getUserInfo(KAKAO_ACCESS_TOKEN))
                .thenReturn(kakaoUser(12345L, "새닉네임", "new-image"));
        when(userRepository.findByKakaoIdAndStatus("12345", UserStatus.ACTIVE)).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createTokenPair(existingUser)).thenReturn(tokenPair());

        LoginResponse response = authService.loginWithKakao(new KakaoLoginRequest(KAKAO_ACCESS_TOKEN));

        assertThat(response.user().nickname()).isEqualTo("커스텀닉네임");
        assertThat(response.user().profileImageUrl()).isEqualTo("/images/profile-defaults/default-1.png");
        assertThat(existingUser.getNickname()).isEqualTo("커스텀닉네임");
        assertThat(existingUser.getProfileImageUrl()).isEqualTo("/images/profile-defaults/default-1.png");
        assertThat(existingUser.getKakaoProfileImageUrl()).isEqualTo("new-image");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginWithKakaoCreatesTemporaryNicknameWhenKakaoNicknameIsMissing() {
        when(kakaoAuthClient.getUserInfo(KAKAO_ACCESS_TOKEN))
                .thenReturn(kakaoUser(12345L, null, null));
        when(userRepository.findByKakaoIdAndStatus("12345", UserStatus.ACTIVE)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("pickit_12345")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });
        when(jwtTokenProvider.createTokenPair(any(User.class))).thenReturn(tokenPair());

        LoginResponse response = authService.loginWithKakao(new KakaoLoginRequest(KAKAO_ACCESS_TOKEN));

        assertThat(response.user().nickname()).isEqualTo("pickit_12345");
    }

    @Test
    void reissueRotatesRefreshTokenWhenStoredTokenIsValid() {
        User user = User.createFromKakao("12345", "민수", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken savedToken = RefreshToken.create(user, "hashed-refresh-token", LocalDateTime.now().plusDays(1));
        TokenPair tokenPair = tokenPair();

        when(jwtTokenProvider.getRefreshTokenUserId("old-refresh-token")).thenReturn(1L);
        when(refreshTokenService.getValidToken("old-refresh-token")).thenReturn(savedToken);
        when(jwtTokenProvider.createTokenPair(user)).thenReturn(tokenPair);

        TokenResponse response = authService.reissue(new RefreshTokenRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(refreshTokenService).rotate(user, "refresh-token", tokenPair.refreshTokenExpiresAt());
    }

    @Test
    void logoutRevokesRefreshToken() {
        authService.logout(new LogoutRequest("refresh-token"));

        verify(refreshTokenService).revoke("refresh-token");
    }

    @Test
    void getCurrentUserReturnsAuthenticatedUserProfile() {
        User user = User.createFromKakao("12345", "민수", "profile-image");
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE)).thenReturn(Optional.of(user));

        assertThat(authService.getCurrentUser(1L).nickname()).isEqualTo("민수");
    }

    private KakaoUserResponse kakaoUser(Long id, String nickname, String profileImageUrl) {
        return new KakaoUserResponse(
                id,
                new KakaoUserResponse.KakaoAccount(
                        new KakaoUserResponse.Profile(nickname, profileImageUrl)
                )
        );
    }

    private TokenPair tokenPair() {
        Instant now = Instant.now();
        return new TokenPair(
                "access-token",
                "refresh-token",
                now.plusSeconds(3600),
                now.plusSeconds(1209600)
        );
    }
}
