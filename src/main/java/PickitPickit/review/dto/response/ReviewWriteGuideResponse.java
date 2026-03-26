package PickitPickit.review.dto.response;

import java.util.List;

public record ReviewWriteGuideResponse(
        Guide reviewGuide,
        Guide bragGuide
) {
    public static ReviewWriteGuideResponse defaultGuide() {
        return new ReviewWriteGuideResponse(
                new Guide(
                        "후기 작성 안내",
                        List.of(
                                "별점은 0.5점부터 5.0점까지, 0.5 단위로 입력할 수 있습니다.",
                                "난이도는 1단계부터 5단계까지 선택할 수 있습니다.",
                                "후기 멘트는 선택 입력입니다.",
                                "매장 경험을 다른 사용자들이 참고할 수 있도록 솔직하게 작성해주세요."
                        )
                ),
                new Guide(
                        "자랑하기 안내",
                        List.of(
                                "자랑하기는 사진 첨부가 필수입니다.",
                                "뽑기에 사용한 비용을 함께 기록할 수 있습니다.",
                                "과도한 비방, 허위 내용, 부적절한 이미지는 제한될 수 있습니다."
                        )
                )
        );
    }

    public record Guide(
            String title,
            List<String> messages
    ) {
    }
}