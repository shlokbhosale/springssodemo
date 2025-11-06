package com.example.springssodemo.service;

import com.example.springssodemo.model.Role;
import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.RoleRepository;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Handles Just-In-Time (JIT) provisioning for new OIDC users.
 */
@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default service to get the OIDC user
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();

        String email = (String) attributes.get("email");
        String username = email; // Use email as the username

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            // --- User does not exist, create them (JIT Provisioning) ---
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setProvider("OIDC"); // Mark as an OIDC user
            newUser.setEnabled(true);

            // Assign default USER role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER", "Default user role")));
            newUser.addRole(userRole);

            userRepository.save(newUser);

            return oidcUser; // Return the original OIDC user

        } else {
            // --- User exists, just log them in ---
            // We could also update their name/picture here if we wanted
            return oidcUser;
        }
    }
}