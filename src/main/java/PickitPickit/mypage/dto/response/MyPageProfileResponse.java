package PickitPickit.mypage.dto.response;

import PickitPickit.user.domain.ProfileImageType;

import java.util.List;

public record MyPageProfileResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String kakaoProfileImageUrl,
        ProfileImageType profileImageType,
        List<MyPageInterestTagResponse> interestTags,
        List<MyPageDefaultProfileImageResponse> defaultProfileImages,
        long reviewCount,
        long bragCount,
        long favoriteStoreCount
) {
}