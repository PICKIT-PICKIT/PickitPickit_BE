package PickitPickit.store.dto;

import PickitPickit.store.domain.StoreManager;
import PickitPickit.store.domain.StoreManagerRole;

public record StoreManagerResponse(
        Long id,
        Long storeId,
        Long userId,
        StoreManagerRole role
) {
    public static StoreManagerResponse from(StoreManager storeManager) {
        return new StoreManagerResponse(
                storeManager.getId(),
                storeManager.getStore().getId(),
                storeManager.getUser().getId(),
                storeManager.getRole()
        );
    }
}
