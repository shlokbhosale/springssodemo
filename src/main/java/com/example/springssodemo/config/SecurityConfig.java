package com.example.springssodemo.config;

import com.example.springssodemo.service.CustomOidcUserService; // <-- IMPORT
import org.springframework.beans.factory.annotation.Autowired; // <-- IMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired // <-- INJECT
    private CustomOidcUserService customOidcUserService;

    @Bean
    public AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler() {
        return new RoleBasedAuthenticationSuccessHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/auth/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/oauth2/**", "/saml2/**").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/home").hasAnyRole("USER", "ADMIN") // Allow admin to see home page
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // ✅ CONFIGURE OIDC LOGIN TO USE OUR JIT SERVICE
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService) // <-- HERE
                        )
                )
                .saml2Login(saml2 -> saml2
                        .loginPage("/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}