package com.robotech.controller;

import com.robotech.exception.BusinessException;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.dto.*;
import com.robotech.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competidor")
@PreAuthorize("hasAnyRole('USUARIO', 'COMPETIDOR')")
public class CompetidorController {
    
    @Autowired
    private RobotService robotService;
    
    @Autowired
    private InvitacionService invitacionService;
    
    @Autowired
    private SolicitudService solicitudService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private RankingService rankingService;
    
    @Autowired
    private InscripcionService inscripcionService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * GET /api/competidor/mi-perfil
     * Obtener perfil del competidor
     */
    @GetMapping("/mi-perfil")
    public ResponseEntity<RobotResponse> obtenerMiPerfil(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        RobotResponse robot = robotService.obtenerMiPerfil(idUsuario);
        return ResponseEntity.ok(robot);
    }
    
    /**
     * GET /api/competidor/robot
     * Ver mi robot con todas las especificaciones
     */
    @GetMapping("/robot")
    public ResponseEntity<RobotResponse> obtenerMiRobot(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        RobotResponse robot = robotService.obtenerRobotCompleto(idUsuario);
        
        return ResponseEntity.ok(robot);
    }
    
    /**
     * POST /api/competidor/robot/especificaciones
     * Completar especificaciones técnicas del robot
     */
    @PostMapping("/robot/especificaciones")
    public ResponseEntity<RobotResponse> completarEspecificaciones(
            @Valid @RequestBody CrearRobotRequest request,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        RobotResponse robot = robotService.completarEspecificaciones(idUsuario, request);
        
        return ResponseEntity.ok(robot);
    }
    
    /**
     * PUT /api/competidor/robot/especificaciones
     * Actualizar especificaciones técnicas del robot
     */
    @PutMapping("/robot/especificaciones")
    public ResponseEntity<RobotResponse> actualizarEspecificaciones(
            @Valid @RequestBody ActualizarRobotRequest request,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        RobotResponse robot = robotService.actualizarEspecificaciones(idUsuario, request);
        
        return ResponseEntity.ok(robot);
    }

    /**
     * POST /api/competidor/salir-club
     * Salir del club voluntariamente
     */
    @PostMapping("/salir-club")
    public ResponseEntity<Void> salirDeClub(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        usuarioService.salirDeClub(idUsuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/competidor/clubes/{clubId}/solicitar
     * Solicitar unirse a un club
     */
    @PostMapping("/clubes/{clubId}/solicitar")
    public ResponseEntity<SolicitudResponse> solicitarUnirse(
            @PathVariable Integer clubId,
            @RequestBody(required = false) SolicitudRequest request,
            Authentication authentication) {
        
        Integer idCompetidor = obtenerIdUsuario(authentication);
        
        if (request == null) {
            request = new SolicitudRequest();
        }
        
        SolicitudResponse response = solicitudService.crearSolicitud(clubId, idCompetidor, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/competidor/solicitudes
     * Obtener solicitudes enviadas
     */
    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudResponse>> obtenerMisSolicitudes(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        List<SolicitudResponse> solicitudes = solicitudService.obtenerMisSolicitudes(idUsuario);
        return ResponseEntity.ok(solicitudes);
    }
    
    /**
     * PUT /api/competidor/solicitudes/{id}/cancelar
     * Cancelar solicitud
     */
    @PutMapping("/solicitudes/{id}/cancelar")
    public ResponseEntity<Void> cancelarSolicitud(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        solicitudService.cancelarSolicitud(id, idUsuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/competidor/invitaciones
     * Obtener invitaciones pendientes
     */
    @GetMapping("/invitaciones")
    public ResponseEntity<List<InvitacionResponse>> obtenerMisInvitaciones(Authentication authentication) {
        String email = authentication.getName();
        List<InvitacionResponse> invitaciones = invitacionService.obtenerMisInvitaciones(email);
        return ResponseEntity.ok(invitaciones);
    }
    
    /**
     * POST /api/competidor/invitaciones/{id}/aceptar
     * Aceptar invitación
     */
    @PostMapping("/invitaciones/{id}/aceptar")
    public ResponseEntity<Void> aceptarInvitacion(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        invitacionService.aceptarInvitacion(id, idUsuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/competidor/invitaciones/{id}/rechazar
     * Rechazar invitación
     */
    @PutMapping("/invitaciones/{id}/rechazar")
    public ResponseEntity<Void> rechazarInvitacion(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        invitacionService.rechazarInvitacion(id, idUsuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/competidor/mi-ranking?categoria_id={id}
     * Obtener ranking del competidor
     */
    @GetMapping("/mi-ranking")
    public ResponseEntity<List<RankingResponse>> obtenerMiRanking(
            @RequestParam("categoria_id") Integer categoriaId,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        List<RankingResponse> ranking = rankingService.obtenerRankingIndividual(categoriaId, idUsuario);
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * POST /api/competidor/inscripciones
     * Solicitar inscripción individual en un torneo
     */
    @PostMapping("/inscripciones")
    public ResponseEntity<InscripcionResponse> solicitarInscripcion(
            @Valid @RequestBody InscripcionRequest request,
            Authentication authentication) {
        
        Integer idUsuario = obtenerIdUsuario(authentication);
        InscripcionResponse response = inscripcionService.solicitarInscripcionIndividual(idUsuario, request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/competidor/inscripciones
     * Ver mis inscripciones
     */
    @GetMapping("/inscripciones")
    public ResponseEntity<List<InscripcionResponse>> obtenerMisInscripciones(Authentication authentication) {
        Integer idUsuario = obtenerIdUsuario(authentication);
        List<InscripcionResponse> inscripciones = inscripcionService.obtenerMisInscripciones(idUsuario);
        
        return ResponseEntity.ok(inscripciones);
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