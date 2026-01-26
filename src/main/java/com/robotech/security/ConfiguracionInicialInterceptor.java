package com.robotech.security;

import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ConfiguracionInicialInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            
            if (usuario != null && usuario.getDebeCambiarPassword()) {
                String requestURI = request.getRequestURI();
                
                if (!requestURI.contains("/configuracion-inicial") && 
                    !requestURI.contains("/logout") &&
                    !requestURI.contains("/auth/")) {
                    
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"success\": false, \"message\": \"Debes completar la configuración inicial antes de continuar\"}"
                    );
                    return false;
                }
            }
        }
        
        return true;
    }
}