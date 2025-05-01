package edu.util;

@SuppressWarnings("HideUtilityClassConstructor")
public class AuthenticationChecker {
    public static boolean checkUserAuthentication(String role) {
        return "USER".equals(role);
    }
}
