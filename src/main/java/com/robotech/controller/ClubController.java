package com.robotech.controller;

import com.robotech.dto.ClubResponse;
import com.robotech.dto.ReenviarClubRequest;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.service.ClubService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clubes")
public class ClubController {
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * POST /api/clubes/{id}/reclamar
     * Reclamar club creado por el admin
     */
    @PostMapping("/{id}/reclamar")
    public ResponseEntity<Map<String, Object>> reclamarClub(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String nombreClub = request.get("nombreClub");
        String descripcion = request.get("descripcion");
        
        if (nombreClub == null || nombreClub.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "El nombre del club es obligatorio"
                ));
        }
        
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            Integer idUsuario = usuario.getIdUsuario();
            
            ClubResponse club = clubService.reclamarClub(id, idUsuario, nombreClub, descripcion);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", club,
                "message", "Club reclamado exitosamente. Ahora está pendiente de validación por el administrador."
            ));
            
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error al reclamar club: " + e.getMessage()
                ));
        }
    }
    
    /**
     * GET /api/clubes/sin-reclamar
     * Obtener el club sin reclamar del usuario actual
     */
    @GetMapping("/sin-reclamar")
    public ResponseEntity<Map<String, Object>> obtenerClubSinReclamar(Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            ClubResponse club = clubService.obtenerClubSinReclamar(usuario.getIdUsuario());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", club
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
                ));
        }
    }

    /**
     * GET /api/clubes/mi-club
     * Obtener el club del usuario actual
     */
    @GetMapping("/mi-club")
    public ResponseEntity<Map<String, Object>> obtenerMiClub(Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            ClubResponse club = clubService.obtenerMiClub(usuario.getIdUsuario());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", club
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
                ));
        }
    }

    /**
     * POST /api/clubes/crear
     * Crear club para usuario autenticado
     */
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearClub(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String nombreClub = request.get("nombreClub");
        String descripcion = request.get("descripcion");
        
        if (nombreClub == null || nombreClub.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "El nombre del club es obligatorio"
                ));
        }
        
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            Integer idUsuario = usuario.getIdUsuario();
            
            ClubResponse club = clubService.crearClubParaUsuario(idUsuario, nombreClub, descripcion);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", club,
                "message", "Club creado exitosamente. Está pendiente de validación por el administrador."
            ));
            
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Error al crear club: " + e.getMessage()
                ));
        }
    }

 /**
  * PUT /api/clubes/{id}/reenviar
  * Reenviar club rechazado para nueva validación
  */
 @PutMapping("/{id}/reenviar")
 public ResponseEntity<Map<String, Object>> reenviarClubParaValidacion(
     @PathVariable Integer id,
     @Valid @RequestBody ReenviarClubRequest request,
     Authentication authentication
 ) {
     try {
         String email = authentication.getName();
         Usuario usuario = usuarioRepository.findByEmail(email)
                 .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
         
         Integer idUsuario = usuario.getIdUsuario();
         
         ClubResponse club = clubService.reenviarClubParaValidacion(id, idUsuario, request);
         
         return ResponseEntity.ok(Map.of(
             "success", true,
             "data", club,
             "message", "Club reenviado exitosamente para validación"
         ));
         
     } catch (IllegalStateException e) {
         return ResponseEntity.status(HttpStatus.FORBIDDEN)
             .body(Map.of(
                 "success", false,
                 "message", e.getMessage()
             ));
             
     } catch (ResourceNotFoundException e) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND)
             .body(Map.of(
                 "success", false,
                 "message", e.getMessage()
             ));
             
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
             .body(Map.of(
                 "success", false,
                 "message", "Error al reenviar club: " + e.getMessage()
             ));
     }
 }
}