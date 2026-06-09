package PickitPickit.store.security;

import PickitPickit.user.domain.UserRole;
import PickitPickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("rolePermissionChecker")
@RequiredArgsConstructor
public class RolePermissionChecker {

    private final UserRepository userRepository;

    public boolean isAdmin(Authentication authentication) {
        Long userId = extractUserId(authentication);

        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    public boolean isStoreOwner(Authentication authentication) {
        Long userId = extractUserId(authentication);

        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.STORE_OWNER)
                .orElse(false);
    }

    public boolean isAdminOrStoreOwner(Authentication authentication) {
        Long userId = extractUserId(authentication);

        if (userId == null) {
            return false;
        }

        return userRepository.findById(userId)
                .map(user ->
                        user.getRole() == UserRole.ADMIN
                                || user.getRole() == UserRole.STORE_OWNER
                )
                .orElse(false);
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