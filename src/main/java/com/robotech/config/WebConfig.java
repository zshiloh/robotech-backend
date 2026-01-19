package com.robotech.config;

import com.robotech.security.ConfiguracionInicialInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de interceptores web
 * Registra el interceptor de configuración inicial
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private ConfiguracionInicialInterceptor configuracionInicialInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(configuracionInicialInterceptor)
                .addPathPatterns("/api/**")  // Aplicar a todas las rutas de API
                .excludePathPatterns(
                    "/api/auth/**",  // Excluir autenticación (login, registro, recuperar password)
                    "/api/usuario/configuracion-inicial"  // Excluir configuración inicial
                );
    }
}