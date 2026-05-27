package PickitPickit.mypage.dto.request;

import java.util.List;

public record MyPageProfileUpdateRequest(
        String nickname,
        String profileImageType,
        String profileImageUrl,
        List<Long> interestTagIds
) {
}