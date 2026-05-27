package PickitPickit.global.security;

import PickitPickit.user.domain.UserStatus;
import PickitPickit.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WithdrawnUserBlockInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Jwt jwt)) {
            return true;
        }

        Long userId = parseUserId(jwt.getSubject());

        if (userId == null) {
            return true;
        }

        boolean active = userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE);

        if (active) {
            return true;
        }

        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"code":"USER_WITHDRAWN","message":"탈퇴한 사용자입니다."}
                """);

        return false;
    }

    private Long parseUserId(String subject) {
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}