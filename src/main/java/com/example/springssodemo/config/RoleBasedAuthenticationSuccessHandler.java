package com.example.springssodemo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Custom AuthenticationSuccessHandler to redirect users based on their roles.
 * Admins are redirected to /admin/dashboard, Users to /home.
 */
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // Use a delegate to handle saved requests (e.g., redirecting to a deep link after login)
    // but fall back to role-based logic if no saved request exists.
    private final AuthenticationSuccessHandler adminHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private final AuthenticationSuccessHandler userHandler = new SavedRequestAwareAuthenticationSuccessHandler();

    public RoleBasedAuthenticationSuccessHandler() {
        ((SavedRequestAwareAuthenticationSuccessHandler) adminHandler).setDefaultTargetUrl("/admin/dashboard");
        ((SavedRequestAwareAuthenticationSuccessHandler) userHandler).setDefaultTargetUrl("/home");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (authentication == null) {
            userHandler.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // Check authorities for ROLE_ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (isAdmin) {
            adminHandler.onAuthenticationSuccess(request, response, authentication);
        } else {
            userHandler.onAuthenticationSuccess(request, response, authentication);
        }
    }
}