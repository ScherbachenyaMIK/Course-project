package edu.util;

import org.springframework.security.core.context.SecurityContextHolder;

@SuppressWarnings("HideUtilityClassConstructor")
public class AuthenticationChecker {
    @SuppressWarnings("MultipleStringLiterals")
    public static boolean checkAuthorities() {
        if ("anonymousUser".equals(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal()
                        .toString())) {
            return checkUserAuthentication("NONE");
        }
        return checkUserAuthentication("USER");
    }

    public static String getCurrentUsername() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if ("anonymousUser".equals(principal.toString())) {
            return null;
        }
        return principal.toString();
    }

    private static boolean checkUserAuthentication(String role) {
        return "USER".equals(role);
    }
}
