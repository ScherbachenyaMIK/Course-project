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

    private static boolean checkUserAuthentication(String role) {
        return "USER".equals(role);
    }
}
