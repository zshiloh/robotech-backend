package com.robotech.controller;

import com.robotech.dto.NotificacionResponse;
import com.robotech.exception.BusinessException;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * GET /api/notificaciones?leidas={bool}&limit={num}
     * Obtener notificaciones del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> obtenerNotificaciones(
            @RequestParam(required = false) Boolean leidas,
            @RequestParam(required = false) Integer limit,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        List<NotificacionResponse> notificaciones = notificacionService.obtenerNotificaciones(idUsuario, leidas, limit);
        return ResponseEntity.ok(notificaciones);
    }
    
    /**
     * GET /api/notificaciones/count?leidas=false
     * Contar notificaciones no leídas
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> contarNoLeidas(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        int count = (int) notificacionService.contarNoLeidas(idUsuario);
        return ResponseEntity.ok(count);
    }
    
    /**
     * PUT /api/notificaciones/{id}/marcar-leida
     * Marcar notificación como leída
     */
    @PutMapping("/{id}/marcar-leida")
    public ResponseEntity<Void> marcarComoLeida(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        notificacionService.marcarComoLeida(id, idUsuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/notificaciones/marcar-todas-leidas
     * Marcar todas las notificaciones como leídas
     */
    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        notificacionService.marcarTodasComoLeidas(idUsuario);
        return ResponseEntity.ok().build();
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