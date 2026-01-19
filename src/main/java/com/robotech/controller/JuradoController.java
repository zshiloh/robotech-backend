package com.robotech.controller;

import com.robotech.dto.EnfrentamientoResponse;
import com.robotech.dto.ResultadoRequest;
import com.robotech.exception.BusinessException;
import com.robotech.model.Enfrentamiento;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.service.EnfrentamientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jurado")
@PreAuthorize("hasRole('JURADO')")
public class JuradoController {
    
    @Autowired
    private EnfrentamientoService enfrentamientoService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * GET /api/jurado/enfrentamientos?categoria_id={id}&estado={estado}
     * Obtener enfrentamientos de una categoría
     */
    @GetMapping("/enfrentamientos")
    public ResponseEntity<List<EnfrentamientoResponse>> obtenerEnfrentamientos(
            @RequestParam("categoria_id") Integer categoriaId,
            @RequestParam(required = false) String estado) {
        
        List<EnfrentamientoResponse> enfrentamientos;
        
        if ("Pendiente".equals(estado)) {
            enfrentamientos = enfrentamientoService.obtenerEnfrentamientosPendientes(categoriaId);
        } else {
            enfrentamientos = enfrentamientoService.obtenerEnfrentamientosPorCategoria(categoriaId);
        }
        
        return ResponseEntity.ok(enfrentamientos);
    }
    
    /**
     * POST /api/jurado/enfrentamientos/{id}/resultado
     * Registrar resultado de un enfrentamiento
     */
    @PostMapping("/enfrentamientos/{id}/resultado")
    public ResponseEntity<Void> registrarResultado(
            @PathVariable Integer id,
            @Valid @RequestBody ResultadoRequest request,
            Authentication authentication) {
        
        Integer idJurado = obtenerIdUsuario(authentication);
        enfrentamientoService.registrarResultado(id, idJurado, request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/jurado/enfrentamientos/{id}
     * Obtener enfrentamiento por ID
     */
    @GetMapping("/enfrentamientos/{id}")
    public ResponseEntity<Enfrentamiento> obtenerEnfrentamiento(@PathVariable Integer id) {
        Enfrentamiento enfrentamiento = enfrentamientoService.obtenerEnfrentamientoPorId(id);
        return ResponseEntity.ok(enfrentamiento);
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