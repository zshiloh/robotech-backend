package com.robotech.controller;

import com.robotech.dto.*;
import com.robotech.exception.BusinessException;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/representante")
@PreAuthorize("hasRole('REPRESENTANTE')")
public class RepresentanteController {
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private InvitacionService invitacionService;
    
    @Autowired
    private SolicitudService solicitudService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private InscripcionService inscripcionService;
    
    /**
     * GET /api/representante/mi-club
     * Obtener club del representante
     */
    @GetMapping("/mi-club")
    public ResponseEntity<ClubResponse> obtenerMiClub(Authentication authentication) {
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse club = clubService.obtenerMiClub(idRepresentante);
        return ResponseEntity.ok(club);
    }
    
    /**
     * POST /api/representante/clubes/{clubId}/invitar
     * Invitar competidor al club
     */
    @PostMapping("/clubes/{clubId}/invitar")
    public ResponseEntity<InvitacionResponse> invitarCompetidor(
            @PathVariable Integer clubId,
            @Valid @RequestBody InvitacionRequest request,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        InvitacionResponse response = invitacionService.crearInvitacion(clubId, idRepresentante, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/representante/solicitudes
     * Obtener solicitudes pendientes del club
     */
    @GetMapping("/solicitudes")
    public ResponseEntity<List<SolicitudResponse>> obtenerSolicitudes(Authentication authentication) {
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse club = clubService.obtenerMiClub(idRepresentante);
        List<SolicitudResponse> solicitudes = solicitudService.obtenerSolicitudesDelClub(club.getIdClub(), idRepresentante);
        return ResponseEntity.ok(solicitudes);
    }
    
    /**
     * POST /api/representante/solicitudes/{id}/aceptar
     * Aceptar solicitud
     */
    @PostMapping("/solicitudes/{id}/aceptar")
    public ResponseEntity<Void> aceptarSolicitud(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        solicitudService.aceptarSolicitud(id, idRepresentante);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/representante/solicitudes/{id}/rechazar
     * Rechazar solicitud
     */
    @PutMapping("/solicitudes/{id}/rechazar")
    public ResponseEntity<Void> rechazarSolicitud(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        solicitudService.rechazarSolicitud(id, idRepresentante);
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/representante/competidores/{idCompetidor}/expulsar
     * Expulsar competidor del club
     */
    @PostMapping("/competidores/{idCompetidor}/expulsar")
    public ResponseEntity<Void> expulsarCompetidor(
            @PathVariable Integer idCompetidor,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse club = clubService.obtenerMiClub(idRepresentante);
        usuarioService.expulsarCompetidor(club.getIdClub(), idRepresentante, idCompetidor);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/representante/invitaciones/{id}/cancelar
     * Cancelar invitación
     */
    @PutMapping("/invitaciones/{id}/cancelar")
    public ResponseEntity<Void> cancelarInvitacion(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        invitacionService.cancelarInvitacion(id, idRepresentante);
        return ResponseEntity.ok().build();
    }
    
    /**
     * PUT /api/representante/clubes/{id}/editar
     * Editar club
     */
    @PutMapping("/clubes/{id}/editar")
    public ResponseEntity<ClubResponse> editarClub(
            @PathVariable Integer id,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String logoUrl,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse response = clubService.editarClub(id, idRepresentante, descripcion, logoUrl);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/representante/clubes/{id}
     * Eliminar club
     */
    @DeleteMapping("/clubes/{id}")
    public ResponseEntity<Void> eliminarClub(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        clubService.eliminarClub(id, idRepresentante);
        return ResponseEntity.ok().build();
    }
    
    /**
     * POST /api/representante/clubes/{id}/reactivar
     * Reactivar club
     */
    @PostMapping("/clubes/{id}/reactivar")
    public ResponseEntity<ClubResponse> reactivarClub(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse response = clubService.reactivarClub(id, idRepresentante);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/representante/invitaciones
     * Obtener invitaciones enviadas por el club
     */
    @GetMapping("/invitaciones")
    public ResponseEntity<List<InvitacionResponse>> obtenerInvitaciones(Authentication authentication) {
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse club = clubService.obtenerMiClub(idRepresentante);
        List<InvitacionResponse> invitaciones = invitacionService.obtenerInvitacionesDelClub(club.getIdClub(), idRepresentante);
        return ResponseEntity.ok(invitaciones);
    }
    
    /**
     * POST /api/representante/torneo/inscribir
     * Inscribir club en torneo
     */
    @PostMapping("/torneo/inscribir")
    public ResponseEntity<String> inscribirClubEnTorneo(
            @Valid @RequestBody InscribirClubRequest request,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        inscripcionService.inscribirClub(request, idRepresentante);
        
        return ResponseEntity.ok("Inscripciones realizadas exitosamente");
    }
    
    /**
     * GET /api/representante/inscripciones-pendientes
     * Ver solicitudes de inscripción pendientes del club
     */
    @GetMapping("/inscripciones-pendientes")
    public ResponseEntity<List<InscripcionResponse>> obtenerInscripcionesPendientes(Authentication authentication) {
        Integer idRepresentante = obtenerIdUsuario(authentication);
        ClubResponse club = clubService.obtenerMiClub(idRepresentante);
        
        List<InscripcionResponse> pendientes = inscripcionService.obtenerInscripcionesPendientes(club.getIdClub());
        
        return ResponseEntity.ok(pendientes);
    }
    
    /**
     * PUT /api/representante/inscripciones/{id}/aprobar
     * Aprobar inscripción individual
     */
    @PutMapping("/inscripciones/{id}/aprobar")
    public ResponseEntity<String> aprobarInscripcion(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        inscripcionService.aprobarInscripcion(id, idRepresentante);
        
        return ResponseEntity.ok("Inscripción aprobada exitosamente");
    }
    
    /**
     * PUT /api/representante/inscripciones/{id}/rechazar
     * Rechazar inscripción individual
     */
    @PutMapping("/inscripciones/{id}/rechazar")
    public ResponseEntity<String> rechazarInscripcion(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        inscripcionService.rechazarInscripcion(id, idRepresentante);
        
        return ResponseEntity.ok("Inscripción rechazada");
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
    
    /**
     * POST /api/representante/invitaciones
     * Enviar invitación (sin necesidad de especificar clubId en URL)
     */
    @PostMapping("/invitaciones")
    public ResponseEntity<InvitacionResponse> enviarInvitacion(
            @Valid @RequestBody InvitacionRequest request,
            Authentication authentication) {
        
        Integer idRepresentante = obtenerIdUsuario(authentication);
        
        // Obtener el club del representante
        ClubResponse club = clubService.obtenerMiClub(idRepresentante);
        
        // Crear la invitación
        InvitacionResponse response = invitacionService.crearInvitacion(
            club.getIdClub(), 
            idRepresentante, 
            request
        );
        
        return ResponseEntity.ok(response);
    }
}