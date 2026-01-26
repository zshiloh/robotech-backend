package com.robotech.service;

import com.robotech.dto.SolicitudRequest;
import com.robotech.dto.SolicitudResponse;
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
import java.util.stream.Collectors;

@Service
public class SolicitudService {
    
    @Autowired
    private SolicitudRepository solicitudRepository;
    
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
     * Crear solicitud para unirse a un club (Usuario/Competidor)
     */
    @Transactional
    public SolicitudResponse crearSolicitud(Integer clubId, Integer idUsuario, SolicitudRequest request) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));

        if (!club.getEstado().getIdEstado().equals(Constants.ESTADO_VALIDADO)) {
            throw new BusinessException("El club no está disponible para recibir solicitudes");
        }

        if (!club.getAceptaSolicitudes()) {
            throw new BusinessException("Este club no acepta solicitudes en este momento");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));

        if (club.getRepresentante().getIdUsuario().equals(idUsuario)) {
            throw new BusinessException("No puedes solicitar unirte a tu propio club");
        }

        robotRepository.findByUsuario_IdUsuario(idUsuario).ifPresent(robot -> {
            if (robot.getClub() != null && robot.getClub().getIdClub().equals(clubId)) {
                throw new BusinessException("Ya perteneces a este club");
            }

            if (robot.getClub() != null && robot.getEstado() == Robot.EstadoRobot.ACTIVO) {
                throw new BusinessException("Ya perteneces a otro club. Debes salir primero antes de solicitar unirte a otro.");
            }
        });

        long competidoresActivos = robotRepository.countRobotsActivosByClubId(clubId);
        if (competidoresActivos >= Constants.MAX_COMPETIDORES_POR_CLUB) {
            throw new BusinessException("El club ha alcanzado el límite de " + Constants.MAX_COMPETIDORES_POR_CLUB + " competidores");
        }

        if (solicitudRepository.existsSolicitudPendiente(idUsuario, clubId, Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Ya tienes una solicitud pendiente para este club");
        }

        Optional<Solicitud> solicitudRechazadaOpt = solicitudRepository.findUltimaSolicitudRechazada(
            idUsuario, 
            clubId, 
            Constants.ESTADO_RECHAZADA
        );

        Solicitud solicitud;

        if (solicitudRechazadaOpt.isPresent()) {
            solicitud = solicitudRechazadaOpt.get();
            
            CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado Pendiente no encontrado"));
            
            solicitud.setEstado(estadoPendiente);
            solicitud.setMensajeOpcional(request.getMensajeOpcional());
            solicitud.setFechaSolicitud(LocalDateTime.now());
            solicitud.setFechaRespuesta(null);
            
            solicitudRepository.save(solicitud);
            
        } else {
            CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado Pendiente no encontrado"));
            
            solicitud = new Solicitud();
            solicitud.setUsuario(usuario);
            solicitud.setClub(club);
            solicitud.setMensajeOpcional(request.getMensajeOpcional());
            solicitud.setEstado(estadoPendiente);
            
            solicitudRepository.save(solicitud);
        }

        notificacionService.crearNotificacion(
            club.getRepresentante().getIdUsuario(),
            Constants.NOTIF_SOLICITUD_RECIBIDA,
            "Nueva solicitud recibida",
            usuario.getNombreCompleto() + " ha solicitado unirse a tu club '" + club.getNombreClub() + "'."
        );

        return convertirAResponse(solicitud);
    }
    
    /**
     * Aceptar solicitud (Representante)
     */
    @Transactional
    public void aceptarSolicitud(Integer solicitudId, Integer idRepresentante) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", solicitudId));
        
        Club club = solicitud.getClub();
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para aceptar esta solicitud");
        }
        
        if (!solicitud.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Esta solicitud ya fue procesada");
        }
        
        Usuario solicitante = solicitud.getUsuario();
        
        long competidoresActivos = robotRepository.countRobotsActivosByClubId(club.getIdClub());
        if (competidoresActivos >= Constants.MAX_COMPETIDORES_POR_CLUB) {
            throw new BusinessException("El club ha alcanzado el límite de competidores");
        }
        
        robotRepository.findByUsuario_IdUsuario(solicitante.getIdUsuario()).ifPresent(robot -> {
            if (robot.getClub() != null && robot.getEstado() == Robot.EstadoRobot.ACTIVO) {
                throw new BusinessException("El solicitante ya pertenece a otro club");
            }
        });
        
        validarCambioClubTrimestral(solicitante, club);
        
        CatEstado estadoAceptada = catEstadoRepository.findById(Constants.ESTADO_ACEPTADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Aceptada no encontrado"));
        
        solicitud.setEstado(estadoAceptada);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        
        robotService.crearRobotAutomatico(solicitante.getIdUsuario(), club.getIdClub());
        
        crearRegistrosHistoricosIniciales(solicitante.getIdUsuario(), club.getIdClub());
        
        asignarRolSiNoTiene(solicitante.getIdUsuario(), Constants.ROL_COMPETIDOR);
        
        String trimestreActual = DateUtils.calcularTrimestreActual();
        
        if (!DateUtils.mismoPeriodoTrimestral(solicitante.getUltimoTrimestreRegistrado(), trimestreActual)) {
            solicitante.setCambiosClubTrimestreActual(1);
            solicitante.setUltimoTrimestreRegistrado(trimestreActual);
        } else {
            solicitante.setCambiosClubTrimestreActual(solicitante.getCambiosClubTrimestreActual() + 1);
        }
        
        solicitante.setCambiosClubTotalesHistorico(solicitante.getCambiosClubTotalesHistorico() + 1);
        solicitante.setFechaUltimoCambioClub(LocalDateTime.now());
        usuarioRepository.save(solicitante);
        
        notificacionService.crearNotificacion(
            solicitante.getIdUsuario(),
            Constants.NOTIF_SOLICITUD_ACEPTADA,
            "Solicitud aceptada",
            "Tu solicitud para unirte al club '" + club.getNombreClub() + "' ha sido aceptada. ¡Bienvenido!"
        );
    }
    
    /**
     * Rechazar solicitud (Representante)
     */
    @Transactional
    public void rechazarSolicitud(Integer solicitudId, Integer idRepresentante) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", solicitudId));
        
        if (!solicitud.getClub().getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para rechazar esta solicitud");
        }
        
        if (!solicitud.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Esta solicitud ya fue procesada");
        }
        
        CatEstado estadoRechazada = catEstadoRepository.findById(Constants.ESTADO_RECHAZADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Rechazada no encontrado"));
        
        solicitud.setEstado(estadoRechazada);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        
        notificacionService.crearNotificacion(
            solicitud.getUsuario().getIdUsuario(),
            Constants.NOTIF_SOLICITUD_RECHAZADA,
            "Solicitud rechazada",
            "Tu solicitud para unirte al club '" + solicitud.getClub().getNombreClub() + "' fue rechazada."
        );
    }
    
    /**
     * Cancelar solicitud (Usuario)
     */
    @Transactional
    public void cancelarSolicitud(Integer solicitudId, Integer idUsuario) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud", "id", solicitudId));
        
        if (!solicitud.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new BusinessException("No tienes permisos para cancelar esta solicitud");
        }
        
        if (!solicitud.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("Solo se pueden cancelar solicitudes pendientes");
        }
        
        CatEstado estadoCancelada = catEstadoRepository.findById(Constants.ESTADO_CANCELADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Cancelada no encontrado"));
        
        solicitud.setEstado(estadoCancelada);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitudRepository.save(solicitud);
    }
    
    /**
     * Obtener TODAS las solicitudes del club (Representante)
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerSolicitudesDelClub(Integer clubId, Integer idRepresentante) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para ver las solicitudes de este club");
        }
        
        List<Solicitud> solicitudes = solicitudRepository.findAllByClubId(clubId);
        
        return solicitudes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener solicitudes enviadas por el usuario (Competidor)
     */
    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerMisSolicitudes(Integer idUsuario) {
        List<Solicitud> solicitudes = solicitudRepository.findByUsuario_IdUsuario(idUsuario);
        
        return solicitudes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Validar RST06: Límite trimestral de cambios de club
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
                        .fechaFin(null) // Activo
                        .build();
                
                puntosClubHistoricoRepository.save(historico);
            }
        }
    }
    
    /**
     * Convertir entidad a DTO
     */
    private SolicitudResponse convertirAResponse(Solicitud solicitud) {
        return new SolicitudResponse(
            solicitud.getIdSolicitud(),                    
            solicitud.getUsuario().getNombreCompleto(),    
            solicitud.getUsuario().getEmail(),             
            solicitud.getUsuario().getIdUsuario(),         
            solicitud.getClub().getNombreClub(),           
            solicitud.getClub().getIdClub(),               
            solicitud.getMensajeOpcional(),                
            solicitud.getEstado().getDescripcion(),        
            solicitud.getFechaSolicitud(),
            solicitud.getFechaRespuesta()
        );
    }
}