package com.example.springssodemo.config;

import com.example.springssodemo.security.DbClientRegistrationRepository; // <-- IMPORT
import com.example.springssodemo.security.DbRelyingPartyRegistrationRepository; // <-- IMPORT
import com.example.springssodemo.service.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomOidcUserService customOidcUserService;

    // ✅ INJECT YOUR DYNAMIC REPOSITORIES
    @Autowired
    private DbClientRegistrationRepository dbClientRegistrationRepository;

    @Autowired
    private DbRelyingPartyRegistrationRepository dbRelyingPartyRegistrationRepository;

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
                        .requestMatchers("/home").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        // ✅ TELL OAUTH2 TO USE YOUR DB-BACKED REPOSITORY
                        .clientRegistrationRepository(dbClientRegistrationRepository)
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                )
                .saml2Login(saml2 -> saml2
                        .loginPage("/login")
                        .successHandler(roleBasedAuthenticationSuccessHandler())
                        // ✅ TELL SAML2 TO USE YOUR DB-BACKED REPOSITORY (This fixes the error)
                        .relyingPartyRegistrationRepository(dbRelyingPartyRegistrationRepository)
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