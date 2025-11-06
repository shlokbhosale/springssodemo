package com.example.springssodemo.controller;

import com.example.springssodemo.dto.UserDto;
import com.example.springssodemo.service.UserService;
import com.example.springssodemo.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Handles local authentication and registration logic.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // ✅ Handle registration form submission
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes) {
        try {
            UserDto userDto = new UserDto();
            userDto.setUsername(username);
            userDto.setEmail(email);
            userDto.setPassword(password);
            userDto.setProvider("LOCAL");
            userDto.setRoles(List.of("ROLE_USER")); // Default to ROLE_USER

            userService.create(userDto);

            // Add success message for the login page
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login?registered=true";

        } catch (ConflictException e) {
            redirectAttributes.addFlashAttribute("error", "Username already exists!");
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred during registration.");
            return "redirect:/register";
        }
    }
}