package PickitPickit.onboarding.dto.request;

import java.util.List;

public record InterestTagUpdateRequest(
        List<Long> tagIds
) {
}
