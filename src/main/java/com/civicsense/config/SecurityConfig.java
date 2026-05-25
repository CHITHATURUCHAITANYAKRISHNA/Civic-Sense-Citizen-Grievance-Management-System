package com.civicsense.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {

            if (authentication == null) {
                response.sendRedirect("/login");
                return;
            }

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            boolean isDepartment = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_DEPARTMENT"));

            if (isAdmin) {
                response.sendRedirect("/admin/dashboard");
            } 
            else if (isDepartment) {
                response.sendRedirect("/department/dashboard");
            } 
            else {
                response.sendRedirect("/user/dashboard");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Public pages
                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/forgot-password",
                        "/reset-password",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**"
                ).permitAll()

                // Admin access (including admin actions under /user/admin/**)
                .requestMatchers("/admin/**", "/user/admin/**").hasRole("ADMIN")

                // Department access
                .requestMatchers("/department/**").hasRole("DEPARTMENT")

                // User access
                .requestMatchers("/user/**").hasRole("USER")

                // Issues accessible by all roles
                .requestMatchers("/issues/**").hasAnyRole("USER", "ADMIN", "DEPARTMENT")

                // Any other request
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            .rememberMe(rm -> rm
                .key("CivicSenseSecretKey123")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
            )

            .exceptionHandling(ex -> ex
                .accessDeniedPage("/403")
            )

            .headers(headers -> headers
                .cacheControl(cache -> cache.disable())
            );

        return http.build();
    }
}