package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             Model model) {
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        User u = new User(username, password, email);
        userRepository.save(u);
        model.addAttribute("message", "Registered successfully. Please login.");
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        var opt = userRepository.findByUsername(username);
        if (opt.isPresent() && opt.get().getPassword().equals(password)) {
            session.setAttribute("username", username);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }
}
