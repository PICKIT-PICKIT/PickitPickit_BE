package PickitPickit.onboarding.service;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.onboarding.domain.InterestTag;
import PickitPickit.onboarding.domain.UserInterestTag;
import PickitPickit.onboarding.dto.request.InterestTagUpdateRequest;
import PickitPickit.onboarding.dto.request.NicknameUpdateRequest;
import PickitPickit.onboarding.dto.request.ProfileImageUpdateRequest;
import PickitPickit.onboarding.dto.response.*;
import PickitPickit.onboarding.repository.InterestTagRepository;
import PickitPickit.onboarding.repository.UserInterestTagRepository;
import PickitPickit.user.domain.ProfileImageType;
import PickitPickit.user.domain.User;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 15;

    private final UserRepository userRepository;
    private final InterestTagRepository interestTagRepository;
    private final UserInterestTagRepository userInterestTagRepository;
    private final DefaultProfileImageCatalog defaultProfileImageCatalog;

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getStatus(Long userId) {
        User user = getUser(userId);
        return OnboardingStatusResponse.of(user, userInterestTagRepository.findByUserId(userId));
    }

    @Transactional
    public OnboardingStatusResponse updateNickname(Long userId, NicknameUpdateRequest request) {
        User user = getUser(userId);
        String nickname = normalizeNickname(request == null ? null : request.nickname());

        if (!isValidNickname(nickname)) {
            throw new ApiException(ErrorStatus.INVALID_NICKNAME);
        }

        if (userRepository.existsByNicknameAndIdNot(nickname, user.getId())) {
            throw new ApiException(ErrorStatus.DUPLICATE_NICKNAME);
        }

        user.updateNickname(nickname);
        return OnboardingStatusResponse.of(user, userInterestTagRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public ProfileImageOptionsResponse getProfileImages(Long userId) {
        User user = getUser(userId);
        return new ProfileImageOptionsResponse(
                user.getKakaoProfileImageUrl(),
                defaultProfileImageCatalog.getDefaultImages()
        );
    }

    @Transactional
    public OnboardingStatusResponse updateProfileImage(Long userId, ProfileImageUpdateRequest request) {
        User user = getUser(userId);
        ProfileImageType type = parseProfileImageType(request == null ? null : request.type());
        String profileImageUrl = request == null ? null : request.profileImageUrl();

        validateProfileImage(user, type, profileImageUrl);
        user.updateProfileImage(type, profileImageUrl);

        return OnboardingStatusResponse.of(user, userInterestTagRepository.findByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<InterestTagResponse> getInterestTags() {
        return interestTagRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(InterestTagResponse::from)
                .toList();
    }

    @Transactional
    public OnboardingStatusResponse updateInterestTags(Long userId, InterestTagUpdateRequest request) {
        User user = getUser(userId);
        List<Long> tagIds = request == null ? null : request.tagIds();

        if (tagIds == null || tagIds.isEmpty()) {
            throw new ApiException(ErrorStatus.INTEREST_TAG_REQUIRED);
        }

        List<Long> uniqueTagIds = new LinkedHashSet<>(tagIds).stream().toList();
        List<InterestTag> interestTags = interestTagRepository.findByIdInAndActiveTrue(uniqueTagIds);

        if (interestTags.size() != uniqueTagIds.size()) {
            throw new ApiException(ErrorStatus.INTEREST_TAG_NOT_FOUND);
        }

        userInterestTagRepository.deleteByUser(user);
        userInterestTagRepository.saveAll(
                interestTags.stream()
                        .map(interestTag -> UserInterestTag.create(user, interestTag))
                        .toList()
        );
        user.markOnboardingIncomplete();

        return OnboardingStatusResponse.of(user, userInterestTagRepository.findByUserId(userId));
    }

    @Transactional
    public OnboardingCompleteResponse complete(Long userId) {
        User user = getUser(userId);

        if (!isValidNickname(user.getNickname())
                || !StringUtils.hasText(user.getProfileImageUrl())
                || !userInterestTagRepository.existsByUserId(userId)) {
            throw new ApiException(ErrorStatus.ONBOARDING_INCOMPLETE);
        }

        user.completeOnboarding();
        return OnboardingCompleteResponse.mainMap();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.USER_NOT_FOUND, "해당 사용자를 찾을 수 없습니다."));
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? null : nickname.trim();
    }

    private boolean isValidNickname(String nickname) {
        return StringUtils.hasText(nickname)
                && nickname.length() >= NICKNAME_MIN_LENGTH
                && nickname.length() <= NICKNAME_MAX_LENGTH;
    }

    private ProfileImageType parseProfileImageType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE_TYPE);
        }

        try {
            return ProfileImageType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE_TYPE);
        }
    }

    private void validateProfileImage(User user, ProfileImageType type, String profileImageUrl) {
        if (!StringUtils.hasText(profileImageUrl)) {
            throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE);
        }

        if (type == ProfileImageType.KAKAO) {
            if (!profileImageUrl.equals(user.getKakaoProfileImageUrl())) {
                throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE);
            }
            return;
        }

        if (type == ProfileImageType.DEFAULT && defaultProfileImageCatalog.containsImageUrl(profileImageUrl)) {
            return;
        }

        throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE);
    }
}
