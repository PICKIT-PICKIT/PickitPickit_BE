package PickitPickit.onboarding.dto.response;

import PickitPickit.onboarding.domain.InterestTag;

public record InterestTagResponse(
        Long id,
        String name
) {

    public static InterestTagResponse from(InterestTag interestTag) {
        return new InterestTagResponse(interestTag.getId(), interestTag.getName());
    }
}
