package com.example.springssodemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ✅ Security Configuration
 * Handles access rules, custom login page, logout behavior,
 * and integration with OAuth2 + SAML2 SSO.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for development and APIs (enable in production if needed)
                .csrf(csrf -> csrf.disable())

                // 🔓 Configure endpoint access permissions
                .authorizeHttpRequests(auth -> auth
                        // Allow all static resources (HTML, JS, CSS, images)
                        .requestMatchers(
                                "/login.html", "/register.html", "/home.html",
                                "/css/**", "/js/**", "/assets/**", "/images/**"
                        ).permitAll()

                        // Allow OAuth2 and SAML SSO paths
                        .requestMatchers("/oauth2/**", "/saml2/**").permitAll()

                        // Allow REST API endpoints for auth (if you use JSON login/register)
                        .requestMatchers("/auth/**", "/api/public/**").permitAll()

                        // Any other endpoint requires authentication
                        .anyRequest().authenticated()
                )

                // 🔐 Configure custom login
                .formLogin(form -> form
                        .loginPage("/login.html")                 // Your custom login page
                        .loginProcessingUrl("/auth/login")        // POST endpoint for login
                        .defaultSuccessUrl("/home.html", true)    // Redirect on successful login
                        .failureUrl("/login.html?error=true")     // Redirect on failure
                        .permitAll()
                )

                // 🔓 Allow logout from anywhere
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // 🧩 Enable OAuth2 (Google, GitHub, etc.)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login.html")
                        .defaultSuccessUrl("/home.html", true)
                )

                // 🧩 Enable SAML2 (miniOrange or other IDP)
                .saml2Login(saml2 -> saml2
                        .loginPage("/login.html")
                        .defaultSuccessUrl("/home.html", true)
                );
                .http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/home/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/login", "/register", "/auth/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
                 );


        // ✅ Build the security chain
        return http.build();
    }
}
