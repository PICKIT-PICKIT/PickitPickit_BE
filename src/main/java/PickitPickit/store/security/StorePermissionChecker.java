package PickitPickit.store.security;

import PickitPickit.global.exception.ApiException;
import PickitPickit.global.response.ErrorStatus;
import PickitPickit.store.repository.StoreManagerRepository;
import PickitPickit.store.repository.StoreProductRepository;
import PickitPickit.user.domain.User;
import PickitPickit.user.domain.UserRole;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("storePermissionChecker")
@RequiredArgsConstructor
public class StorePermissionChecker {

    private final UserRepository userRepository;
    private final StoreManagerRepository storeManagerRepository;
    private final StoreProductRepository storeProductRepository;

    public boolean canManageStore(Authentication authentication, Long storeId) {
        if (storeId == null) {
            return false;
        }

        Long userId = extractUserId(authentication);
        if (userId == null) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        if (user.getRole() != UserRole.STORE_OWNER) {
            return false;
        }

        return storeManagerRepository.existsByStoreIdAndUserId(storeId, userId);
    }

    public boolean canManageStoreProduct(Authentication authentication, Long storeProductId) {
        if (storeProductId == null) {
            return false;
        }

        Long storeId = storeProductRepository.findStoreIdById(storeProductId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND, "매장 상품을 찾을 수 없습니다."));

        return canManageStore(authentication, storeId);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            return null;
        }
        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
