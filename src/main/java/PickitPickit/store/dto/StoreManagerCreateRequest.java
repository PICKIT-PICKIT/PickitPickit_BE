package PickitPickit.store.dto;

import PickitPickit.store.domain.StoreManagerRole;
import jakarta.validation.constraints.NotNull;

public record StoreManagerCreateRequest(

        @NotNull(message = "storeId는 필수입니다.")
        Long storeId,

        @NotNull(message = "userId는 필수입니다.")
        Long userId,

        StoreManagerRole role
) {
}
