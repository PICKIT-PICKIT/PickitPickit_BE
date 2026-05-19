package PickitPickit.onboarding.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.onboarding.domain.InterestTag;
import PickitPickit.onboarding.dto.request.InterestTagUpdateRequest;
import PickitPickit.onboarding.dto.request.NicknameUpdateRequest;
import PickitPickit.onboarding.dto.request.ProfileImageUpdateRequest;
import PickitPickit.onboarding.repository.InterestTagRepository;
import PickitPickit.onboarding.repository.UserInterestTagRepository;
import PickitPickit.user.domain.ProfileImageType;
import PickitPickit.user.domain.User;
import PickitPickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InterestTagRepository interestTagRepository;

    @Mock
    private UserInterestTagRepository userInterestTagRepository;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(
                userRepository,
                interestTagRepository,
                userInterestTagRepository,
                new DefaultProfileImageCatalog()
        );
    }

    @Test
    void updateNicknameSavesTrimmedNickname() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIdNot("새닉네임", 1L)).thenReturn(false);
        when(userInterestTagRepository.findByUserId(1L)).thenReturn(List.of());

        onboardingService.updateNickname(1L, new NicknameUpdateRequest(" 새닉네임 "));

        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    void updateNicknameFailsWhenNicknameIsInvalid() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.updateNickname(1L, new NicknameUpdateRequest("a")))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.INVALID_NICKNAME));
    }

    @Test
    void updateNicknameFailsWhenNicknameIsDuplicated() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNicknameAndIdNot("중복닉네임", 1L)).thenReturn(true);

        assertThatThrownBy(() -> onboardingService.updateNickname(1L, new NicknameUpdateRequest("중복닉네임")))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.DUPLICATE_NICKNAME));
    }

    @Test
    void updateProfileImageSavesDefaultImageWhenUrlIsAllowed() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userInterestTagRepository.findByUserId(1L)).thenReturn(List.of());

        onboardingService.updateProfileImage(
                1L,
                new ProfileImageUpdateRequest("DEFAULT", "/images/profile-defaults/default-1.png")
        );

        assertThat(user.getProfileImageType()).isEqualTo(ProfileImageType.DEFAULT);
        assertThat(user.getProfileImageUrl()).isEqualTo("/images/profile-defaults/default-1.png");
    }

    @Test
    void updateProfileImageFailsWhenUrlIsNotAllowed() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.updateProfileImage(
                1L,
                new ProfileImageUpdateRequest("DEFAULT", "https://evil.example/profile.png")
        ))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.INVALID_PROFILE_IMAGE));
    }

    @Test
    void updateProfileImageSavesKakaoImageWhenUrlMatches() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userInterestTagRepository.findByUserId(1L)).thenReturn(List.of());

        onboardingService.updateProfileImage(
                1L,
                new ProfileImageUpdateRequest("KAKAO", "kakao-image")
        );

        assertThat(user.getProfileImageType()).isEqualTo(ProfileImageType.KAKAO);
        assertThat(user.getProfileImageUrl()).isEqualTo("kakao-image");
    }

    @Test
    void updateInterestTagsFailsWhenEmpty() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> onboardingService.updateInterestTags(1L, new InterestTagUpdateRequest(List.of())))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.INTEREST_TAG_REQUIRED));
    }

    @Test
    void updateInterestTagsFailsWhenTagDoesNotExist() {
        User user = user();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(interestTagRepository.findByIdInAndActiveTrue(List.of(1L, 99L))).thenReturn(List.of(interestTag(1L)));

        assertThatThrownBy(() -> onboardingService.updateInterestTags(1L, new InterestTagUpdateRequest(List.of(1L, 99L))))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.INTEREST_TAG_NOT_FOUND));
    }

    @Test
    void completeSucceedsWhenRequiredFieldsExist() {
        User user = user();
        user.updateProfileImage(ProfileImageType.DEFAULT, "/images/profile-defaults/default-1.png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userInterestTagRepository.existsByUserId(1L)).thenReturn(true);

        assertThat(onboardingService.complete(1L).next()).isEqualTo("MAIN_MAP");
        assertThat(user.isOnboardingCompleted()).isTrue();
    }

    @Test
    void completeFailsWhenTagIsMissing() {
        User user = user();
        user.updateProfileImage(ProfileImageType.DEFAULT, "/images/profile-defaults/default-1.png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userInterestTagRepository.existsByUserId(1L)).thenReturn(false);

        assertThatThrownBy(() -> onboardingService.complete(1L))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.ONBOARDING_INCOMPLETE));
    }

    private User user() {
        User user = User.createFromKakao("12345", "민수", "kakao-image");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private InterestTag interestTag(Long id) {
        try {
            var constructor = InterestTag.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            InterestTag interestTag = constructor.newInstance();
            ReflectionTestUtils.setField(interestTag, "id", id);
            ReflectionTestUtils.setField(interestTag, "name", "포켓몬");
            ReflectionTestUtils.setField(interestTag, "displayOrder", 1);
            ReflectionTestUtils.setField(interestTag, "active", true);
            return interestTag;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
