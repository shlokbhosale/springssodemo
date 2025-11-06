package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ✅ Unified Page Controller.
 * Handles navigation and page rendering for all Thymeleaf templates.
 * Replaces the need for HomeController.
 */
@Controller
public class PageController {

    // ✅ Redirect default route to login
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // ✅ Custom login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ✅ Registration page
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * ✅ User Home Page
     * Secured by SecurityConfig to only allow ROLE_USER.
     * We can inject the @AuthenticationPrincipal (our User object) directly.
     */
    @GetMapping("/home")
    public String homePage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "home";
    }

    /**
     * ✅ Admin Dashboard Page
     * Secured by SecurityConfig to only allow ROLE_ADMIN.
     * We inject the principal just to show "Welcome, Admin!"
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        // The admin-dashboard.html is mostly client-side JavaScript
        // No model attributes are needed for it to function
        return "admin-dashboard";
    }
}