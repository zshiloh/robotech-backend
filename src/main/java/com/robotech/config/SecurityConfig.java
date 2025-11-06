package com.robotech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad
 * Permite acceso público a todos los endpoints (para desarrollo/pruebas)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()   // Permitir todas las peticiones
            );
        
        return http.build();
    }
}