package com.robotech.service;

import com.robotech.dto.InvitacionRequest;
import com.robotech.dto.InvitacionResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.*;
import com.robotech.repository.*;
import com.robotech.util.Constants;
import com.robotech.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Comparator;

@Service
public class InvitacionService {
    
    @Autowired
    private InvitacionRepository invitacionRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private UsuarioRolRepository usuarioRolRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private RobotService robotService;
    
    @Autowired
    private PuntosClubHistoricoRepository puntosClubHistoricoRepository;
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    

    /**
     * Crear invitación (Representante)
     */
    @Transactional
    public InvitacionResponse crearInvitacion(Integer clubId, Integer idRepresentante, InvitacionRequest request) {
        
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para invitar en nombre de este club");
        }
        
        if (!club.getEstado().getIdEstado().equals(Constants.ESTADO_VALIDADO)) {
            throw new BusinessException("El club debe estar validado para enviar invitaciones");
        }
        
        Usuario destinatario = usuarioRepository.findByEmail(request.getEmailDestinatario())
                .orElseThrow(() -> new BusinessException("El email '" + request.getEmailDestinatario() + "' no está registrado en el sistema"));
        
        if (destinatario.getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No puedes invitarte a ti mismo");
        }
        
        robotRepository.findByUsuario_IdUsuario(destinatario.getIdUsuario()).ifPresent(robot -> {
            if (robot.getClub() != null && robot.getClub().getIdClub().equals(clubId)) {
                throw new BusinessException("Este usuario ya pertenece a tu club");
            }
        });
        
        long competidoresActivos = robotRepository.countRobotsActivosByClubId(clubId);
        if (competidoresActivos >= Constants.MAX_COMPETIDORES_POR_CLUB) {
            throw new BusinessException("El club ha alcanzado el límite de " + Constants.MAX_COMPETIDORES_POR_CLUB + " competidores");
        }
        
        long invitacionesPendientes = invitacionRepository.countInvitacionesPendientesByClubId(
            clubId, 
            Constants.ESTADO_PENDIENTE
        );
        
        if (invitacionesPendientes >= Constants.MAX_INVITACIONES_PENDIENTES) {
            throw new BusinessException("Has alcanzado el límite de " + Constants.MAX_INVITACIONES_PENDIENTES + " invitaciones pendientes");
        }
        
        List<Invitacion> invitacionesExistentes = invitacionRepository
                .findInvitacionesPendientesByClubId(clubId, Constants.ESTADO_PENDIENTE);
        
        boolean yaInvitado = invitacionesExistentes.stream()
                .anyMatch(inv -> inv.getEmailDestinatario().equals(request.getEmailDestinatario()));
        
        if (yaInvitado) {
            throw new BusinessException("Ya existe una invitación pendiente para este email");
        }
        
        CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Pendiente no encontrado"));
        
        Invitacion invitacion = new Invitacion();
        invitacion.setClub(club);
        invitacion.setEmailDestinatario(request.getEmailDestinatario());
        invitacion.setToken(UUID.randomUUID().toString());
        invitacion.setEstado(estadoPendiente);
        
        invitacionRepository.save(invitacion);
        
        notificacionService.crearNotificacion(
            destinatario.getIdUsuario(),
            Constants.NOTIF_INVITACION_CLUB,
            "Invitación a club",
            "El club '" + club.getNombreClub() + "' te ha invitado a unirte como competidor. [Invitación #" + invitacion.getIdInvitacion() + "]"
        );
        
        if (club.getEstadoActividad() == Club.EstadoActividad.INACTIVO_7D || 
            club.getEstadoActividad() == Club.EstadoActividad.INACTIVO) {
            club.setEstadoActividad(Club.EstadoActividad.ACTIVO);
            clubRepository.save(club);
        }
        
        return convertirAResponse(invitacion);
    }
    
    /**
     * Aceptar invitación (Usuario)
     */
    @Transactional
    public void aceptarInvitacion(Integer invitacionId, Integer idUsuario) {
        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación", "id", invitacionId));
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        if (!invitacion.getEmailDestinatario().equals(usuario.getEmail())) {
            throw new BusinessException("Esta invitación no es para ti");
        }
        
        if (!invitacion.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Esta invitación ya fue procesada");
        }
        
        Club club = invitacion.getClub();
        
        if (!club.getEstado().getIdEstado().equals(Constants.ESTADO_VALIDADO)) {
            throw new BusinessException("El club ya no está activo");
        }
        
        long competidoresActivos = robotRepository.countRobotsActivosByClubId(club.getIdClub());
        if (competidoresActivos >= Constants.MAX_COMPETIDORES_POR_CLUB) {
            throw new BusinessException("El club ha alcanzado el límite de competidores");
        }
        
        robotRepository.findByUsuario_IdUsuario(idUsuario).ifPresent(robot -> {
            if (robot.getClub() != null && robot.getEstado() == Robot.EstadoRobot.ACTIVO) {
                throw new BusinessException("Ya perteneces a un club. Debes salir primero antes de aceptar esta invitación.");
            }
        });
        
        validarCambioClubTrimestral(usuario, club);
        
        CatEstado estadoAceptada = catEstadoRepository.findById(Constants.ESTADO_ACEPTADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Aceptada no encontrado"));
        
        invitacion.setEstado(estadoAceptada);
        invitacion.setFechaRespuesta(LocalDateTime.now());
        invitacionRepository.save(invitacion);
        
        robotService.crearRobotAutomatico(idUsuario, club.getIdClub());
        
        crearRegistrosHistoricosIniciales(idUsuario, club.getIdClub());
        
        if (club.getEstadoActividad() == Club.EstadoActividad.INACTIVO_7D || 
            club.getEstadoActividad() == Club.EstadoActividad.INACTIVO) {
            club.setEstadoActividad(Club.EstadoActividad.ACTIVO);
            clubRepository.save(club);
        }
        
        asignarRolSiNoTiene(idUsuario, Constants.ROL_COMPETIDOR);
        
        String trimestreActual = DateUtils.calcularTrimestreActual();
        
        if (!DateUtils.mismoPeriodoTrimestral(usuario.getUltimoTrimestreRegistrado(), trimestreActual)) {
            usuario.setCambiosClubTrimestreActual(1);
            usuario.setUltimoTrimestreRegistrado(trimestreActual);
        } else {
            usuario.setCambiosClubTrimestreActual(usuario.getCambiosClubTrimestreActual() + 1);
        }
        
        usuario.setCambiosClubTotalesHistorico(usuario.getCambiosClubTotalesHistorico() + 1);
        usuario.setFechaUltimoCambioClub(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        notificacionService.crearNotificacion(
            club.getRepresentante().getIdUsuario(),
            Constants.NOTIF_INVITACION_ACEPTADA,
            "Invitación aceptada",
            usuario.getNombreCompleto() + " ha aceptado la invitación y se unió a tu club."
        );
    }
    
    /**
     * Rechazar invitación (Usuario)
     */
    @Transactional
    public void rechazarInvitacion(Integer invitacionId, Integer idUsuario) {
        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación", "id", invitacionId));
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        if (!invitacion.getEmailDestinatario().equals(usuario.getEmail())) {
            throw new BusinessException("Esta invitación no es para ti");
        }
        
        if (!invitacion.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Esta invitación ya fue procesada");
        }
        
        CatEstado estadoRechazada = catEstadoRepository.findById(Constants.ESTADO_RECHAZADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Rechazada no encontrado"));
        
        invitacion.setEstado(estadoRechazada);
        invitacion.setFechaRespuesta(LocalDateTime.now());
        invitacionRepository.save(invitacion);
    }
    
    /**
     * Cancelar invitación (Representante)
     */
    @Transactional
    public void cancelarInvitacion(Integer invitacionId, Integer idRepresentante) {
        Invitacion invitacion = invitacionRepository.findById(invitacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitación", "id", invitacionId));
        
        if (!invitacion.getClub().getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para cancelar esta invitación");
        }
        
        if (!invitacion.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Solo se pueden cancelar invitaciones pendientes");
        }
        
        CatEstado estadoCancelada = catEstadoRepository.findById(Constants.ESTADO_CANCELADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Cancelada no encontrado"));
        
        invitacion.setEstado(estadoCancelada);
        invitacion.setFechaRespuesta(LocalDateTime.now());
        invitacionRepository.save(invitacion);
    }
    
    /**
     * Obtener invitaciones pendientes del usuario
     */
    @Transactional(readOnly = true)
    public List<InvitacionResponse> obtenerMisInvitaciones(String email) {
        List<Invitacion> invitaciones = invitacionRepository.findInvitacionesPendientesByEmail(
            email, 
            Constants.ESTADO_PENDIENTE
        );
        
        return invitaciones.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener TODAS las invitaciones enviadas por el club (Representante)
     */
    @Transactional(readOnly = true)
    public List<InvitacionResponse> obtenerInvitacionesDelClub(Integer clubId, Integer idRepresentante) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para ver las invitaciones de este club");
        }
        
        List<Invitacion> todasLasInvitaciones = invitacionRepository.findAllInvitacionesByClubId(clubId);
        
        Map<String, Invitacion> invitacionesPorEmail = todasLasInvitaciones.stream()
                .collect(Collectors.toMap(
                    Invitacion::getEmailDestinatario,
                    inv -> inv,
                    (inv1, inv2) -> inv1.getFechaEnvio().isAfter(inv2.getFechaEnvio()) ? inv1 : inv2
                ));
        
        // Convertir a lista y ordenar por fecha (más recientes primero)
        List<Invitacion> invitacionesUnicas = invitacionesPorEmail.values().stream()
                .sorted(Comparator.comparing(Invitacion::getFechaEnvio).reversed())
                .collect(Collectors.toList());
        
        return invitacionesUnicas.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Límite trimestral de cambios de club
     */
    private void validarCambioClubTrimestral(Usuario usuario, Club clubDestino) {
        String trimestreActual = DateUtils.calcularTrimestreActual();
        
        if (clubDestino.getEstadoActividad() == Club.EstadoActividad.INACTIVO || 
            clubDestino.getEstadoActividad() == Club.EstadoActividad.INACTIVO_7D) {
            return;
        }
        
        if (!DateUtils.mismoPeriodoTrimestral(usuario.getUltimoTrimestreRegistrado(), trimestreActual)) {
            return;
        }
        
        if (usuario.getCambiosClubTrimestreActual() >= Constants.MAX_CAMBIOS_CLUB_TRIMESTRE) {
            throw new BusinessException(
                "Has alcanzado el límite de " + Constants.MAX_CAMBIOS_CLUB_TRIMESTRE + 
                " cambios de club en este trimestre. Podrás cambiar nuevamente en el próximo trimestre."
            );
        }
        
        if (usuario.getFechaUltimoCambioClub() != null) {
            long diasDesdeUltimoCambio = DateUtils.diasEntre(
                usuario.getFechaUltimoCambioClub(), 
                LocalDateTime.now()
            );
            
            if (diasDesdeUltimoCambio < Constants.DIAS_ESPERA_ENTRE_CAMBIOS) {
                long diasRestantes = Constants.DIAS_ESPERA_ENTRE_CAMBIOS - diasDesdeUltimoCambio;
                LocalDateTime proximaFecha = usuario.getFechaUltimoCambioClub()
                        .plusDays(Constants.DIAS_ESPERA_ENTRE_CAMBIOS);
                
                throw new BusinessException(
                    "Debes esperar " + Constants.DIAS_ESPERA_ENTRE_CAMBIOS + " días entre cada cambio de club. " +
                    "Podrás cambiar nuevamente el " + proximaFecha.toLocalDate()
                );
            }
        }
    }
    
    /**
     * Asignar rol a usuario si no lo tiene
     */
    private void asignarRolSiNoTiene(Integer idUsuario, Integer idRol) {
        if (!usuarioRolRepository.existsByUsuarioIdAndRolId(idUsuario, idRol)) {
            UsuarioRol.UsuarioRolId usuarioRolId = new UsuarioRol.UsuarioRolId(idUsuario, idRol);
            UsuarioRol usuarioRol = new UsuarioRol();
            usuarioRol.setId(usuarioRolId);
            usuarioRolRepository.save(usuarioRol);
        }
    }
    
    /**
     * Crear registros históricos iniciales cuando un competidor se une a un club
     */
    private void crearRegistrosHistoricosIniciales(Integer idUsuario, Integer idClub) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByUsuario_IdUsuario(idUsuario);
        
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", idClub));
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        for (Inscripcion inscripcion : inscripciones) {
        	
            Optional<PuntosClubHistorico> registroExistente = 
                puntosClubHistoricoRepository.findActivoByUsuarioAndClubAndCategoria(
                    idUsuario, 
                    idClub, 
                    inscripcion.getCategoria().getIdCategoria()
                );
            
            if (registroExistente.isEmpty()) {
            	
                PuntosClubHistorico historico = PuntosClubHistorico.builder()
                        .club(club)
                        .usuario(usuario)
                        .categoria(inscripcion.getCategoria())
                        .puntosGanadosAqui(0)
                        .fechaInicio(LocalDateTime.now())
                        .fechaFin(null)
                        .build();
                
                puntosClubHistoricoRepository.save(historico);
            }
        }
    }
    
    /**
     * Convertir entidad a DTO
     */
    private InvitacionResponse convertirAResponse(Invitacion invitacion) {
        return new InvitacionResponse(
            invitacion.getIdInvitacion(),
            invitacion.getClub().getNombreClub(),
            invitacion.getClub().getIdClub(),
            invitacion.getEmailDestinatario(),
            invitacion.getToken(),
            invitacion.getEstado().getDescripcion(),
            invitacion.getFechaEnvio(),
            invitacion.getFechaRespuesta()
        );
    }
}