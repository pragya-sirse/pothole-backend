package com.pothole.pothole_backend.config;


import com.pothole.pothole_backend.security.CustomUserDetailsService;
import com.pothole.pothole_backend.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disable - REST API hai
                .csrf(csrf -> csrf.disable())

                // Session stateless - JWT use kar rahe hain
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Frame options disable - Render/proxy ke liye
                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable()))

                // HTTPS redirect disable - Render khud handle karta hai
                .requiresChannel(channel ->
                        channel.anyRequest().requiresInsecure())

                // Public aur protected routes
                .authorizeHttpRequests(auth -> auth

                        // ── AUTH ── public
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── LOCATIONS ── public
                        .requestMatchers("/api/locations/**").permitAll()

                        // ── POTHOLES ── public GET only
                        .requestMatchers(HttpMethod.GET, "/api/potholes/map/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/potholes/city/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/potholes/zone/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/potholes/{id}").permitAll()

                        // ── EVERYTHING ELSE ── needs login
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}