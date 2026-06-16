package com.example.chatapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Keep disabled while developing

                /*
                 * --- Smart CORS Configuration ---
                 * Uses allowedOriginPatterns with allowCredentials(true)
                 * to safely accept large base64 payloads from WebSockets/frontend
                 */
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of("*"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))

                .authorizeHttpRequests(auth -> auth
                        // FIXED: explicitly allow the HTML file and the registration endpoint
                        .requestMatchers("/login.html", "/api/auth/register", "/css/**", "/js/**", "/chat/room/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login.html") // Point to our custom page
                        // FIXED: Matches the form action="/login" in login.html
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/index.html", true) // Where to go after success
                        .failureUrl("/login.html?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessUrl("/login.html?logout=true")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}