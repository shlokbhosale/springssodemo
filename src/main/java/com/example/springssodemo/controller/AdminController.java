//package com.example.springssodemo.controller;
//
//import com.example.springssodemo.model.Role;
//import com.example.springssodemo.model.User;
//import com.example.springssodemo.repo.RoleRepository;
//import com.example.springssodemo.repo.UserRepository;
//import com.example.springssodemo.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
///**
// * Handles all admin operations for user and role management.
// */
//@Controller
//@RequestMapping("/admin")
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminController {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private UserService userService;
//
//    // ✅ Load admin dashboard page
//    @GetMapping("/dashboard")
//    public String dashboard(Model model) {
//        List<User> users = userRepository.findAll();
//        List<Role> roles = roleRepository.findAll();
//        model.addAttribute("users", users);
//        model.addAttribute("roles", roles);
//        return "admin-dashboard";
//    }
//
//    // ✅ Promote/demote user
//    @PostMapping("/update-role")
//    public String updateUserRole(@RequestParam Long userId, @RequestParam Long roleId) {
//        Optional<User> userOpt = userRepository.findById(userId);
//        Optional<Role> roleOpt = roleRepository.findById(roleId);
//
//        if (userOpt.isPresent() && roleOpt.isPresent()) {
//            User user = userOpt.get();
//            Role role = roleOpt.get();
//
//            // Clear existing roles and assign new
//            user.getUserRoles().clear();
//            user.addRole(role);
//            userRepository.save(user);
//        }
//
//        return "redirect:/admin/dashboard?updated=true";
//    }
//
//    // ✅ Delete user
//    @PostMapping("/delete-user")
//    public String deleteUser(@RequestParam Long userId) {
//        userRepository.deleteById(userId);
//        return "redirect:/admin/dashboard?deleted=true";
//    }
//
//    // ✅ Add new user (Admin-only)
//    @PostMapping("/add-user")
//    public String addUser(@RequestParam String username,
//                          @RequestParam String email,
//                          @RequestParam String password) {
//        User user = new User();
//        user.setUsername(username);
//        user.setEmail(email);
//        user.setPassword(password);
//        user.setProvider("LOCAL");
//
//        userService.registerUser(user);
//        return "redirect:/admin/dashboard?added=true";
//    }
//}
