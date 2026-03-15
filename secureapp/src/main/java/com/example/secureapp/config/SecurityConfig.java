package com.example.secureapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Completely disable CSRF (essential for H2 and Postman)
                .csrf(csrf -> csrf.disable())

                // 2. Allow frames (essential for H2 UI)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 3. Define the Permit Rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll() // Must be before anyRequest()
                        .requestMatchers("/api/register", "/api/login", "/api/mfa/**").permitAll()
                        .requestMatchers("/api/dashboard").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}