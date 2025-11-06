package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

/**
 * Handles local authentication and registration logic.
 * Works alongside SSO authentication (OAuth2 + SAML2).
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Handle registration form submission
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) {
        try {
            if (userService.existsByUsername(username)) {
                model.addAttribute("error", "Username already exists!");
                return "register";
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setProvider("LOCAL");

            userService.saveUser(user);

            model.addAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration.");
            return "register";
        }
    }

    // ✅ Handles form login (delegated to Spring Security)
    @PostMapping("/login")
    public String login() {
        // Spring Security handles authentication.
        return "redirect:/home";
    }

    // ✅ Handles logout redirection
    @GetMapping("/logout-success")
    public String logoutSuccess(Model model) {
        model.addAttribute("message", "You have been logged out successfully.");
        return "login";
    }
}
