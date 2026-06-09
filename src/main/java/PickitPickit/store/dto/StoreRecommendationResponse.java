package PickitPickit.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Claude 기반 AI 매장 추천 응답")
public record StoreRecommendationResponse(
        @Schema(description = "추천 대상 매장 정보. 응답 배열 순서가 추천 우선순위입니다.")
        StoreResponse store,

        @Schema(
                description = "추천 사유. Claude 호출 실패 시에는 서버 fallback 추천 사유가 반환됩니다.",
                example = "관심 태그인 포켓몬과 가까운 위치를 기준으로 추천합니다."
        )
        String recommendationReason
) {
}
