package PickitPickit.auth.service;

import PickitPickit.auth.client.KakaoAuthClient;
import PickitPickit.auth.domain.RefreshToken;
import PickitPickit.auth.dto.client.KakaoUserResponse;
import PickitPickit.auth.dto.request.KakaoLoginRequest;
import PickitPickit.auth.dto.request.LogoutRequest;
import PickitPickit.auth.dto.request.RefreshTokenRequest;
import PickitPickit.auth.dto.response.LoginResponse;
import PickitPickit.auth.dto.response.TokenResponse;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.security.JwtTokenProvider;
import PickitPickit.global.security.TokenPair;
import PickitPickit.user.domain.User;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(userRepository.findByKakaoId("12345")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });
        when(jwtTokenProvider.createTokenPair(1L)).thenReturn(tokenPair);

        LoginResponse response = authService.loginWithKakao(new KakaoLoginRequest(KAKAO_ACCESS_TOKEN));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().nickname()).isEqualTo("민수");
        assertThat(response.user().profileImageUrl()).isNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getKakaoId()).isEqualTo("12345");
        assertThat(userCaptor.getValue().getProfileImageUrl()).isNull();
        verify(refreshTokenService).rotate(userCaptor.getValue(), "refresh-token", tokenPair.refreshTokenExpiresAt());
    }

    @Test
    void loginWithKakaoReusesExistingUserAndUpdatesProfile() {
        User existingUser = User.createFromKakao("12345", "예전닉네임", "old-image");
        ReflectionTestUtils.setField(existingUser, "id", 1L);

        when(kakaoAuthClient.getUserInfo(KAKAO_ACCESS_TOKEN))
                .thenReturn(kakaoUser(12345L, "새닉네임", "new-image"));
        when(userRepository.findByKakaoId("12345")).thenReturn(Optional.of(existingUser));
        when(jwtTokenProvider.createTokenPair(1L)).thenReturn(tokenPair());

        LoginResponse response = authService.loginWithKakao(new KakaoLoginRequest(KAKAO_ACCESS_TOKEN));

        assertThat(response.user().nickname()).isEqualTo("새닉네임");
        assertThat(response.user().profileImageUrl()).isEqualTo("new-image");
        assertThat(existingUser.getNickname()).isEqualTo("새닉네임");
        assertThat(existingUser.getProfileImageUrl()).isEqualTo("new-image");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginWithKakaoFailsWhenNicknameIsMissing() {
        when(kakaoAuthClient.getUserInfo(KAKAO_ACCESS_TOKEN))
                .thenReturn(kakaoUser(12345L, null, null));

        assertThatThrownBy(() -> authService.loginWithKakao(new KakaoLoginRequest(KAKAO_ACCESS_TOKEN)))
                .isInstanceOf(ApiException.class)
                .hasMessage("카카오 닉네임 동의가 필요합니다.");

        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).createTokenPair(any());
    }

    @Test
    void reissueRotatesRefreshTokenWhenStoredTokenIsValid() {
        User user = User.createFromKakao("12345", "민수", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken savedToken = RefreshToken.create(user, "hashed-refresh-token", LocalDateTime.now().plusDays(1));
        TokenPair tokenPair = tokenPair();

        when(jwtTokenProvider.getRefreshTokenUserId("old-refresh-token")).thenReturn(1L);
        when(refreshTokenService.getValidToken("old-refresh-token")).thenReturn(savedToken);
        when(jwtTokenProvider.createTokenPair(1L)).thenReturn(tokenPair);

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

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

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
        return new TokenPair("access-token", "refresh-token", Instant.now().plusSeconds(1209600));
    }
}
