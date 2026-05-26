package PickitPickit.store.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StoreTagReplaceRequest(
        @NotNull(message = "tags는 필수입니다. 전체 삭제는 빈 배열을 전달하세요.")
        List<@Size(max = 50, message = "태그명은 50자 이하여야 합니다.") String> tags
) {
}
