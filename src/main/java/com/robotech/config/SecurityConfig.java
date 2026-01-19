package com.robotech.config;

import com.robotech.security.CustomUserDetailsService;
import com.robotech.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Order(1)
public class SecurityConfig {
    
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfig corsConfig;
    
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         CorsConfig corsConfig) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfig = corsConfig;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authBuilder
            .userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
        
        return authBuilder.build();
    }
    
    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .csrf(csrf -> csrf.disable())
            // CORS configurado
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Permitir preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Rutas públicas
                .requestMatchers("/api/auth/**", "/api/public/**", "/auth/**", "/public/**", "/error").permitAll()
                // El resto requiere autenticación
                .anyRequest().authenticated()
            )
            // Stateless session
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}