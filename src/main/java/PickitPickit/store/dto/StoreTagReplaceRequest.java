package PickitPickit.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StoreTagReplaceRequest(
        @Schema(description = "매장 대표 태그명 목록. 전체 삭제는 빈 배열([])을 전달합니다.", example = "[\"드래곤볼\", \"인기\"]")
        @NotNull(message = "tags는 필수입니다. 전체 삭제는 빈 배열을 전달하세요.")
        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
