package com.robotech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir solicitudes desde el frontend
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5500",   // Live Server puerto 5500
            "http://localhost:5501",   // Live Server puerto 5501
            "http://localhost:5502",   // Live Server puerto 5502
            "http://127.0.0.1:5500",   // Live Server alternativo
            "http://127.0.0.1:5501",   // Live Server alternativo
            "http://127.0.0.1:5502"    // Live Server alternativo
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Tiempo de caché para preflight requests
        configuration.setMaxAge(3600L);
        
        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}