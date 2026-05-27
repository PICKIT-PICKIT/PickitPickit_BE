package PickitPickit.mypage.service;

import PickitPickit.auth.repository.RefreshTokenRepository;
import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.mypage.dto.request.MyPageProfileUpdateRequest;
import PickitPickit.mypage.dto.response.MyPageDefaultProfileImageResponse;
import PickitPickit.mypage.dto.response.MyPageInterestTagResponse;
import PickitPickit.mypage.dto.response.MyPageProfileResponse;
import PickitPickit.onboarding.domain.InterestTag;
import PickitPickit.onboarding.domain.UserInterestTag;
import PickitPickit.onboarding.repository.InterestTagRepository;
import PickitPickit.onboarding.repository.UserInterestTagRepository;
import PickitPickit.onboarding.service.DefaultProfileImageCatalog;
import PickitPickit.review.repository.BragRepository;
import PickitPickit.review.repository.ReviewRepository;
import PickitPickit.search.service.SearchLogService;
import PickitPickit.store.repository.FavoriteStoreRepository;
import PickitPickit.user.domain.ProfileImageType;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 15;

    private final UserRepository userRepository;
    private final InterestTagRepository interestTagRepository;
    private final UserInterestTagRepository userInterestTagRepository;
    private final DefaultProfileImageCatalog defaultProfileImageCatalog;
    private final ReviewRepository reviewRepository;
    private final BragRepository bragRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SearchLogService searchLogService;

    @Override
    public MyPageProfileResponse getProfile(Long userId) {
        User user = getActiveUser(userId);
        return toResponse(user);
    }

    @Override
    @Transactional
    public MyPageProfileResponse updateProfile(Long userId, MyPageProfileUpdateRequest request) {
        if (request == null) {
            throw new ApiException(ErrorStatus.INVALID_INPUT, "수정할 프로필 정보가 필요합니다.");
        }

        User user = getActiveUser(userId);

        String nickname = normalizeNickname(request.nickname());
        validateNickname(nickname, user.getId());

        ProfileImageType profileImageType = parseProfileImageType(request.profileImageType());
        String resolvedProfileImageUrl = resolveProfileImageUrl(
                user,
                profileImageType,
                request.profileImageUrl()
        );

        List<InterestTag> interestTags = getValidInterestTags(request.interestTagIds());

        user.changeNickname(nickname);
        user.changeProfileImage(profileImageType, resolvedProfileImageUrl);

        userInterestTagRepository.deleteByUser(user);
        userInterestTagRepository.flush();

        userInterestTagRepository.saveAll(
                interestTags.stream()
                        .map(interestTag -> UserInterestTag.create(user, interestTag))
                        .toList()
        );

        return toResponse(user);
    }

    @Override
    @Transactional
    public void withdraw(Long userId) {
        User user = getActiveUser(userId);

        refreshTokenRepository.deleteByUserId(userId);
        userInterestTagRepository.deleteByUser(user);
        favoriteStoreRepository.deleteByUserId(userId);
        searchLogService.clearRecentSearches(userId);

        user.withdraw();
    }

    private User getActiveUser(Long userId) {
        return userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(
                        ErrorStatus.USER_NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));
    }

    private String normalizeNickname(String nickname) {
        return nickname == null ? null : nickname.trim();
    }

    private void validateNickname(String nickname, Long userId) {
        if (!StringUtils.hasText(nickname)
                || nickname.length() < NICKNAME_MIN_LENGTH
                || nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new ApiException(ErrorStatus.INVALID_NICKNAME);
        }

        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new ApiException(ErrorStatus.DUPLICATE_NICKNAME);
        }
    }

    private ProfileImageType parseProfileImageType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE_TYPE);
        }

        try {
            ProfileImageType profileImageType = ProfileImageType.valueOf(
                    type.trim().toUpperCase(Locale.ROOT)
            );

            if (profileImageType != ProfileImageType.KAKAO
                    && profileImageType != ProfileImageType.DEFAULT) {
                throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE_TYPE);
            }

            return profileImageType;
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE_TYPE);
        }
    }

    private String resolveProfileImageUrl(
            User user,
            ProfileImageType profileImageType,
            String requestedProfileImageUrl
    ) {
        if (profileImageType == ProfileImageType.KAKAO) {
            if (!StringUtils.hasText(user.getKakaoProfileImageUrl())) {
                throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE);
            }

            return user.getKakaoProfileImageUrl();
        }

        if (profileImageType == ProfileImageType.DEFAULT) {
            if (!StringUtils.hasText(requestedProfileImageUrl)
                    || !defaultProfileImageCatalog.containsImageUrl(requestedProfileImageUrl)) {
                throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE);
            }

            return requestedProfileImageUrl;
        }

        throw new ApiException(ErrorStatus.INVALID_PROFILE_IMAGE_TYPE);
    }

    private List<InterestTag> getValidInterestTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new ApiException(ErrorStatus.INTEREST_TAG_REQUIRED);
        }

        List<Long> uniqueTagIds = new LinkedHashSet<>(tagIds).stream().toList();
        List<InterestTag> interestTags = interestTagRepository.findByIdInAndActiveTrue(uniqueTagIds);

        if (interestTags.size() != uniqueTagIds.size()) {
            throw new ApiException(ErrorStatus.INTEREST_TAG_NOT_FOUND);
        }

        return interestTags;
    }

    private MyPageProfileResponse toResponse(User user) {
        List<UserInterestTag> selectedUserInterestTags =
                userInterestTagRepository.findByUserId(user.getId());

        Set<Long> selectedTagIds = selectedUserInterestTags.stream()
                .map(userInterestTag -> userInterestTag.getInterestTag().getId())
                .collect(Collectors.toSet());

        List<MyPageInterestTagResponse> interestTags =
                interestTagRepository.findByActiveTrueOrderByDisplayOrderAsc()
                        .stream()
                        .map(interestTag -> new MyPageInterestTagResponse(
                                interestTag.getId(),
                                interestTag.getName(),
                                selectedTagIds.contains(interestTag.getId())
                        ))
                        .toList();

        List<MyPageDefaultProfileImageResponse> defaultProfileImages =
                defaultProfileImageCatalog.getDefaultImages()
                        .stream()
                        .map(defaultImage -> new MyPageDefaultProfileImageResponse(
                                defaultImage.id(),
                                defaultImage.imageUrl(),
                                user.getProfileImageType() == ProfileImageType.DEFAULT
                                        && defaultImage.imageUrl().equals(user.getProfileImageUrl())
                        ))
                        .toList();

        return new MyPageProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getKakaoProfileImageUrl(),
                user.getProfileImageType(),
                interestTags,
                defaultProfileImages,
                reviewRepository.countByUserId(user.getId()),
                bragRepository.countByUserId(user.getId()),
                favoriteStoreRepository.countByUserId(user.getId())
        );
    }
}