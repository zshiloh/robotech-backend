package com.robotech.controller;

import com.robotech.dto.ConfiguracionInicialRequest;
import com.robotech.dto.EditarPerfilUsuarioRequest;
import com.robotech.dto.UsuarioResponse;
import com.robotech.exception.BusinessException;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuario")
@PreAuthorize("isAuthenticated()")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * GET /api/usuario/mi-informacion
     * Obtener información del usuario autenticado
     */
    @GetMapping("/mi-informacion")
    public ResponseEntity<UsuarioResponse> obtenerMiInformacion(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        UsuarioResponse usuario = usuarioService.obtenerMiInformacion(idUsuario);
        
        return ResponseEntity.ok(usuario);
    }
    
    /**
     * PUT /api/usuario/perfil
     * Editar perfil del usuario
     */
    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> editarPerfil(
            @Valid @RequestBody EditarPerfilUsuarioRequest request,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        UsuarioResponse usuario = usuarioService.actualizarPerfil(idUsuario, request);
        
        return ResponseEntity.ok(usuario);
    }
    
    /**
     * PUT /api/usuario/cambiar-password
     * Cambiar contraseña del usuario
     */
    @PutMapping("/cambiar-password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String passwordActual = request.get("passwordActual");
        String passwordNueva = request.get("passwordNueva");
        
        if (passwordActual == null || passwordActual.trim().isEmpty()) {
            throw new BusinessException("La contraseña actual es obligatoria");
        }
        
        if (passwordNueva == null || passwordNueva.trim().isEmpty()) {
            throw new BusinessException("La nueva contraseña es obligatoria");
        }
        
        if (passwordNueva.length() < 6) {
            throw new BusinessException("La nueva contraseña debe tener al menos 6 caracteres");
        }
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        usuarioService.cambiarPassword(idUsuario, passwordActual, passwordNueva);
        
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }
    
    /**
	 * PUT /api/usuario/configuracion-inicial
	 * Completar configuración inicial del usuario
	 */
    @PutMapping("/configuracion-inicial")
    public ResponseEntity<Map<String, String>> configuracionInicial(
            @RequestBody ConfiguracionInicialRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        usuarioService.configuracionInicial(email, request);
        
        return ResponseEntity.ok(Map.of("message", "Configuración completada exitosamente"));
    }
    
    /**
     * Método auxiliar para obtener ID del usuario autenticado
     */
    private Integer obtenerIdUsuario(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        return usuario.getIdUsuario();
    }
}