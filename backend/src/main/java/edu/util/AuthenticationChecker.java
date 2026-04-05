package edu.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@SuppressWarnings("HideUtilityClassConstructor")
public class AuthenticationChecker {
    public static boolean checkAuthorities() {
        return isAuthenticated();
    }

    public static String getCurrentUsername() {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getName();
    }

    public static boolean hasRole(String role) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return false;
        }
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private static boolean isAuthenticated() {
        return currentAuthentication() != null;
    }

    private static Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return authentication;
    }
}
