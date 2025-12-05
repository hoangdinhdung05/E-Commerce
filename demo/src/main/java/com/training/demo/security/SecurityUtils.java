package com.training.demo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        throw new RuntimeException("No authenticated user found");
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getUser().getId();
    }

    public static boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_" + roleName));
    }

    public static String getCurrentUsername() {
        return getCurrentUserDetails().getUsername();
    }

    public static String getCurrentEmail() {
        return getCurrentUserDetails().getUser().getEmail();
    }
}
