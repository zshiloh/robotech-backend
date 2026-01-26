package com.robotech.controller;

import com.robotech.dto.ClubResponse;
import com.robotech.dto.ValidarClubRequest;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.Club;
import com.robotech.model.Robot;
import com.robotech.model.Usuario;
import com.robotech.service.ClubService;
import com.robotech.service.UsuarioService;
import com.robotech.dto.SedeRequest;
import com.robotech.dto.SedeResponse;
import com.robotech.dto.UsuarioResponse;
import com.robotech.dto.TorneoResponse;
import com.robotech.service.SedeService;
import com.robotech.service.TorneoService;
import com.robotech.repository.RobotRepository;
import com.robotech.repository.UsuarioRepository;
import com.robotech.repository.ClubRepository;
import com.robotech.repository.EnfrentamientoRepository;
import com.robotech.repository.NotificacionRepository;
import com.robotech.repository.PuntosClubHistoricoRepository;
import com.robotech.util.Constants;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private SedeService sedeService;
    
    @Autowired
    private TorneoService torneoService;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private NotificacionRepository notificacionRepository;
    
    @Autowired
    private PuntosClubHistoricoRepository puntosClubHistoricoRepository;
    
    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;
    
    /**
	 * POST /api/admin/sedes
	 * Crear una nueva sede
	 */
    @PostMapping("/sedes")
    public ResponseEntity<SedeResponse> crearSede(@Valid @RequestBody SedeRequest request) {
        SedeResponse sede = sedeService.crearSede(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sede);
    }
    
    /**
     * PUT /api/admin/sedes/{id}
     * Actualizar una sede existente
     */
    @PutMapping("/sedes/{id}")
    public ResponseEntity<SedeResponse> actualizarSede(
            @PathVariable Integer id,
            @Valid @RequestBody SedeRequest request) {
        
        SedeResponse sede = sedeService.actualizarSede(id, request);
        return ResponseEntity.ok(sede);
    }
    
    /**
	 * DELETE /api/admin/sedes/{id}
	 * Eliminar una sede
	 */
    @DeleteMapping("/sedes/{id}")
    public ResponseEntity<Map<String, String>> eliminarSede(@PathVariable Integer id) {
        sedeService.eliminarSede(id);
        return ResponseEntity.ok(Map.of("mensaje", "Sede eliminada exitosamente"));
    }
    
    
    /** GET /api/admin/clubes/pendientes
	 * Obtener lista de clubes pendientes de validación
	 */
    @GetMapping("/clubes/pendientes")
    public ResponseEntity<List<ClubResponse>> obtenerClubesPendientes() {
        List<ClubResponse> clubes = clubService.obtenerClubesPendientes();
        return ResponseEntity.ok(clubes);
    }
    
    /** GET /api/admin/clubes
	 * Obtener lista de todos los clubes
	 */
    @GetMapping("/clubes")
    public ResponseEntity<List<ClubResponse>> obtenerTodosLosClubes() {
        List<ClubResponse> clubes = clubService.obtenerTodosLosClubes();
        return ResponseEntity.ok(clubes);
    }

    /** GET /api/admin/usuarios
     * Obtener lista de todos los usuarios
     */
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodosLosUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }
    
    /** GET /api/admin/usuarios/count
	 * Obtener el total de usuarios registrados
	 */
    @GetMapping("/usuarios/count")
    public ResponseEntity<Map<String, Long>> obtenerTotalUsuarios() {
        long total = usuarioService.obtenerTotalUsuarios();
        return ResponseEntity.ok(Map.of("total", total));
    }
    
    /** POST /api/admin/clubes/{id}/validar
     * Validar o rechazar un club
     */
    @PostMapping("/clubes/{id}/validar")
    public ResponseEntity<ClubResponse> validarClub(
            @PathVariable Integer id,
            @Valid @RequestBody ValidarClubRequest request) {
        ClubResponse response = clubService.validarClub(id, request);
        return ResponseEntity.ok(response);
    }
    
    
    /** POST /api/admin/usuarios/{idUsuario}/roles/{idRol}/asignar
	 * Asignar un rol a un usuario
	 */
    @PostMapping("/usuarios/{idUsuario}/roles/{idRol}/asignar")
    public ResponseEntity<Void> asignarRol(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRol,
            Authentication authentication) {
        
        usuarioService.asignarRol(idUsuario, idRol, 1);
        return ResponseEntity.ok().build();
    }
    
    
    /** DELETE /api/admin/usuarios/{idUsuario}/roles/{idRol}
     * Quitar un rol a un usuario
	 */
    @DeleteMapping("/usuarios/{idUsuario}/roles/{idRol}")
    public ResponseEntity<Void> quitarRol(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRol,
            Authentication authentication) {
        
        usuarioService.quitarRol(idUsuario, idRol, 1);
        return ResponseEntity.ok().build();
    }
    
    /**
     * GET /api/admin/estadisticas
     * KPIs para el dashboard
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        long totalUsuarios = usuarioService.obtenerTotalUsuarios();
        long totalClubes = clubService.obtenerTodosLosClubes().size();
        long clubesPendientes = clubService.obtenerClubesPendientes().size();
        long totalTorneos = torneoService.obtenerTodosLosTorneos().size();
        long torneosActivos = torneoService.obtenerTorneosPorEstado(Constants.ESTADO_EN_CURSO).size();
        long totalRobots = robotRepository.count();
        long combatesFinalizados = enfrentamientoRepository.countByEstadoCombate("Finalizado");
        
        Map<String, Object> estadisticas = Map.of(
            "totalUsuarios", totalUsuarios,
            "totalClubes", totalClubes,
            "clubesPendientes", clubesPendientes,
            "totalTorneos", totalTorneos,
            "torneosActivos", torneosActivos,
            "totalRobots", totalRobots,
            "combatesFinalizados", combatesFinalizados
        );
        
        return ResponseEntity.ok(estadisticas);
    }
    
    /**
     * POST /api/admin/clubes
     * Crear club para un usuario (solo con email)
     */
    @PostMapping("/clubes")
    public ResponseEntity<Map<String, Object>> crearClubParaUsuario(
            @RequestBody Map<String, String> request) {
        
        String emailUsuario = request.get("emailUsuario");
        
        if (emailUsuario == null || emailUsuario.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "El email del usuario es obligatorio"));
        }
        
        try {
            ClubResponse club = clubService.crearClubPorAdmin(emailUsuario);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", club,
                "message", "Club creado exitosamente. El usuario recibirá una notificación."
            ));
            
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
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
     * PUT /api/admin/usuarios/{id}/roles
     * Actualizar roles de un usuario
     */
    @PutMapping("/usuarios/{id}/roles")
    public ResponseEntity<Map<String, String>> actualizarRoles(
            @PathVariable Integer id,
            @RequestBody Map<String, List<String>> request) {
        
        List<String> roles = request.get("roles");
        
        if (roles == null) {
            roles = new ArrayList<>();
        }
        
        try {
            usuarioService.actualizarRoles(id, roles);
            return ResponseEntity.ok(Map.of("message", "Roles actualizados exitosamente"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error al actualizar roles: " + e.getMessage()));
        }
    }
    
    /**
     * POST /api/admin/usuarios
     * Crear un nuevo usuario por el admin
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> crearUsuario(
            @RequestBody Map<String, String> request) {
        
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "El email es obligatorio"));
        }
        
        try {
            Map<String, Object> resultado = usuarioService.crearUsuarioPorAdmin(email);
            return ResponseEntity.ok(resultado);
        } catch (BusinessException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/admin/usuarios/{id}
     * Eliminar un usuario verificando dependencias
     */
    @DeleteMapping("/usuarios/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Integer id) {
        try {
        	
            // Verificar que no sea el admin actual
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailActual = auth.getName();
            Usuario usuarioActual = usuarioRepository.findByEmail(emailActual)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario actual no encontrado"));
            
            if (usuarioActual.getIdUsuario().equals(id)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "No puedes eliminar tu propia cuenta"));
            }
            
            // Verificar que el usuario existe
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

            List<String> razonesBloqueo = new ArrayList<>();
            
            // Verificar si tiene robot
            if (robotRepository.existsByUsuario_IdUsuario(id)) {
                Robot robot = robotRepository.findByUsuario_IdUsuario(id).orElse(null);
                
                if (robot != null) {
                    // Verificar si el robot tiene inscripciones
                    razonesBloqueo.add(
                        "Tiene un robot registrado ('" + robot.getNombreRobot() + "'). " +
                        "Los robots deben eliminarse manualmente primero."
                    );
                }
            }
            
            // Verificar si es representante
            if (clubRepository.existsByRepresentante_IdUsuario(id)) {
                List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(id);
                
                if (clubes != null && !clubes.isEmpty()) {
                    Club club = clubes.get(0);
                    String estadoClub = club.getEstado().getDescripcion();
                    
                    // Estado: Rechazado permite eliminación automática
                    if ("Rechazado".equals(estadoClub)) {
                    } else if ("Pendiente".equals(estadoClub)) {
                        razonesBloqueo.add(
                            "Es representante del club '" + club.getNombreClub() + 
                            "' que está pendiente de validación. " +
                            "No se puede eliminar hasta que el club sea validado o rechazado."
                        );
                    } else if ("Validado".equals(estadoClub)) {
                        razonesBloqueo.add(
                            "Es representante del club '" + club.getNombreClub() + "' (validado y activo). " +
                            "Debes cambiar el representante del club antes de eliminar este usuario."
                        );
                    } else {
                        razonesBloqueo.add(
                            "Es representante del club '" + club.getNombreClub() + "'. " +
                            "Debes cambiar el representante primero."
                        );
                    }
                }
            }
            
            // Verificar si tiene historial de puntos
            if (puntosClubHistoricoRepository.existsByUsuario_IdUsuario(id)) {
                long cantidadRegistros = puntosClubHistoricoRepository.countByUsuario_IdUsuario(id);
                razonesBloqueo.add(
                    "Tiene " + cantidadRegistros + " registro(s) de historial de puntos en clubes. " +
                    "Estos datos son permanentes."
                );
            }
            
            // Verificar jurado con combates registrados
            if (enfrentamientoRepository.existsByJuradoRegistro_IdUsuario(id)) {
                long combatesRegistrados = enfrentamientoRepository.countByJuradoRegistro_IdUsuario(id);
                razonesBloqueo.add("Registró resultados de " + combatesRegistrados + " combate(s) como jurado. Este historial debe preservarse.");
            }
            
            // Si tiene dependencias CRÍTICAS, NO permitir eliminación
            if (!razonesBloqueo.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "message", 
                        "❌ No se puede eliminar el usuario porque:\n\n• " + 
                        String.join("\n\n• ", razonesBloqueo) + "\n\n" +
                        "💡 Solo se pueden eliminar usuarios que no tengan historial de participación."
                    ));
            }
            
            List<String> datosEliminados = new ArrayList<>();
            
            // Eliminar robot si existe
            if (robotRepository.existsByUsuario_IdUsuario(id)) {
                Robot robot = robotRepository.findByUsuario_IdUsuario(id).orElse(null);
                if (robot != null) {
                    robotRepository.delete(robot);
                    datosEliminados.add("Robot '" + robot.getNombreRobot() + "'");
                }
            }
            
            // Eliminar notificaciones
            if (notificacionRepository.existsByUsuario_IdUsuario(id)) {
                long cantidad = notificacionRepository.countByUsuario_IdUsuario(id);
                notificacionRepository.deleteByUsuario_IdUsuario(id);
                datosEliminados.add(cantidad + " notificación(es)");
            }

            // Eliminar club rechazado si existe
            if (clubRepository.existsByRepresentante_IdUsuario(id)) {
                List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(id);
                if (clubes != null && !clubes.isEmpty()) {
                    Club club = clubes.get(0);
                    if ("Rechazado".equals(club.getEstado().getDescripcion())) {
                        clubRepository.delete(club);
                        datosEliminados.add("Club rechazado '" + club.getNombreClub() + "'");
                    }
                }
            }

            // Eliminar usuario
            usuarioRepository.deleteById(id);
            
            String mensaje = "✅ Usuario '" + usuario.getNombreCompleto() + "' eliminado exitosamente";
            
            if (!datosEliminados.isEmpty()) {
                mensaje += "\n\n📋 Datos eliminados:\n• " + String.join("\n• ", datosEliminados);
            }
            
            return ResponseEntity.ok(Map.of("message", mensaje));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "❌ Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "❌ Error al eliminar usuario: " + e.getMessage()));
        }
    }
    
    /**
	 * GET /api/admin/usuarios/{id}/club
	 * Obtener el club asociado a un usuario
	 */
    @GetMapping("/usuarios/{id}/club")
    public ResponseEntity<Map<String, Object>> obtenerClubDeUsuario(@PathVariable Integer id) {
        try {
            List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(id);
            
            if (clubes == null || clubes.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Usuario no tiene club"
                ));
            }
            
            Club club = clubes.get(0);
            
            // Crear respuesta
            Map<String, Object> clubData = new HashMap<>();
            clubData.put("idClub", club.getIdClub());
            clubData.put("nombreClub", club.getNombreClub());
            clubData.put("descripcion", club.getDescripcion());
            clubData.put("estado", club.getEstado() != null ? club.getEstado().getDescripcion() : "Desconocido");
            clubData.put("logoUrl", club.getLogoUrl());
            clubData.put("fechaCreacion", club.getFechaCreacion());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", clubData
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/admin/torneos
     * Lista de todos los torneos
     */
    @GetMapping("/torneos")
    public ResponseEntity<List<TorneoResponse>> obtenerTodosLosTorneos() {
        List<TorneoResponse> torneos = torneoService.obtenerTodosLosTorneos();
        return ResponseEntity.ok(torneos);
    }
}