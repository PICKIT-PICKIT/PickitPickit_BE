package PickitPickit.onboarding.dto.response;

import PickitPickit.onboarding.domain.UserInterestTag;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.ProfileImageType;

import java.util.Comparator;
import java.util.List;

public record OnboardingStatusResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String kakaoProfileImageUrl,
        ProfileImageType profileImageType,
        List<InterestTagResponse> selectedTags,
        boolean onboardingCompleted
) {

    public static OnboardingStatusResponse of(User user, List<UserInterestTag> userInterestTags) {
        List<InterestTagResponse> selectedTags = userInterestTags.stream()
                .map(UserInterestTag::getInterestTag)
                .sorted(Comparator.comparingInt(interestTag -> interestTag.getDisplayOrder()))
                .map(InterestTagResponse::from)
                .toList();

        return new OnboardingStatusResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getKakaoProfileImageUrl(),
                user.getProfileImageType(),
                selectedTags,
                user.isOnboardingCompleted()
        );
    }
}
