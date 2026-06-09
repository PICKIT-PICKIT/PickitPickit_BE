package PickitPickit.mypage.dto.response;

public record MyPageInterestTagResponse(
        Long id,
        String name,
        boolean selected
) {
}