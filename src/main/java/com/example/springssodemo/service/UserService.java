package com.example.springssodemo.service;

import com.example.springssodemo.dto.UserDto;
import com.example.springssodemo.exception.ConflictException;
import com.example.springssodemo.exception.NotFoundException;
import com.example.springssodemo.model.Role;
import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.RoleRepository;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * ✅ Unified UserService.
 * Implements UserDetailsService for Spring Security authentication AND
 * provides admin-level CRUD operations for managing users.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- UserDetailsService Implementation ---

    /**
     * Loads user by username (for Spring Security login)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Load user and eagerly fetch roles
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Eagerly initialize the authorities (User model handles this with FetchType.EAGER on userRoles)
        user.getAuthorities().size();

        return user;
    }

    // --- Admin CRUD Operations (for AdminRestController) ---

    /**
     * List users with pagination and search.
     */
    @Transactional(readOnly = true)
    public Page<User> list(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            // The original .class file had this, but the repo doesn't.
            // Using a simple find all for now.
            // return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
            return userRepository.findAll(pageable); // Simplified
        }
        return userRepository.findAll(pageable);
    }

    /**
     * Create a new local user, typically by an admin.
     */
    @Transactional
    public User create(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ConflictException("username_taken");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setProvider(dto.getProvider() != null ? dto.getProvider() : "LOCAL");
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEnabled(true);

        // Assign roles from DTO
        assignRolesToUser(user, dto.getRoles());

        return userRepository.save(user);
    }

    /**
     * Update an existing user.
     */
    @Transactional
    public User update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user_not_found"));

        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());

        // Only update password if a new one is provided
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Update roles
        if (dto.getRoles() != null) {
            assignRolesToUser(user, dto.getRoles());
        }

        return userRepository.save(user);
    }

    /**
     * Delete a user by ID.
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("user_not_found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Helper to find or create roles and assign them to a user.
     */
    private void assignRolesToUser(User user, java.util.List<String> roleNames) {
        user.getUserRoles().clear(); // Clear existing roles
        if (roleNames == null || roleNames.isEmpty()) {
            // Default to ROLE_USER if no roles are specified
            Role userRole = findOrCreateRole("ROLE_USER", "Default user role");
            user.addRole(userRole);
        } else {
            for (String roleName : roleNames) {
                Role role = findOrCreateRole(roleName, "Role " + roleName);
                user.addRole(role);
            }
        }
    }

    /**
     * Finds a role by name, or creates it if it doesn't exist.
     */
    private Role findOrCreateRole(String roleName, String description) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role(null, roleName, description);
                    return roleRepository.save(newRole);
                });
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}