package PickitPickit.onboarding.dto.response;

import java.util.List;

public record ProfileImageOptionsResponse(
        String kakaoProfileImageUrl,
        List<DefaultProfileImageResponse> defaultImages
) {
}
