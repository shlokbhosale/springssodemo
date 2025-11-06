package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Handles navigation and page rendering for Thymeleaf templates.
 */
@Controller
public class PageController {

    private final UserRepository userRepository;

    public PageController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Redirect default route
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

    // ✅ Home page (loads logged-in user data)
    @GetMapping("/home")
    public String homePage(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        Object principal = authentication.getPrincipal();
        User user = new User();

        if (principal instanceof User) {
            user = (User) principal;
        } else if (principal instanceof OAuth2User oauthUser) {
            user.setUsername(oauthUser.getAttribute("name"));
            user.setEmail(oauthUser.getAttribute("email"));
            user.setProvider("OAUTH2");
        } else if (principal instanceof Saml2AuthenticatedPrincipal samlUser) {
            user.setUsername(samlUser.getName());
            user.setEmail(samlUser.getFirstAttribute("email"));
            user.setProvider("SAML2");
        }

        model.addAttribute("user", user);
        return "home";
    }
}
