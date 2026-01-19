package com.robotech.service;

import com.robotech.dto.ClubResponse;
import com.robotech.dto.ClubSimpleResponse;
import com.robotech.dto.ReenviarClubRequest;
import com.robotech.dto.ValidarClubRequest;
import com.robotech.dto.ClubHistorialResponse;
import com.robotech.repository.PuntosClubHistoricoRepository;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.*;
import com.robotech.repository.*;
import com.robotech.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClubService {
    
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
    private TorneoRepository torneoRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private RobotService robotService;
    
    @Autowired
    private PuntosClubHistoricoRepository puntosClubHistoricoRepository;
    
    /**
     * Crear club por admin para usuario
     */
    @Transactional
    public ClubResponse crearClubPorAdmin(String emailUsuario) {
    	
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new BusinessException("El usuario con email " + emailUsuario + " no existe"));
        
        List<Club> clubesExistentes = clubRepository.findByRepresentante_IdUsuario(usuario.getIdUsuario());
        
        for (Club clubExistente : clubesExistentes) {
            String estadoDesc = clubExistente.getEstado().getDescripcion();
            
            if ("Sin Reclamar".equals(estadoDesc)) {
                throw new BusinessException("Este usuario ya tiene un club sin reclamar");
            }
            if ("Pendiente".equals(estadoDesc)) {
                throw new BusinessException("Este usuario ya tiene un club pendiente de validación");
            }
            if ("Validado".equals(estadoDesc)) {
                throw new BusinessException("Este usuario ya tiene un club validado");
            }
        }
        
        CatEstado estadoSinReclamar = catEstadoRepository.findById(Constants.ESTADO_SIN_RECLAMAR)
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Sin Reclamar' no encontrado en la base de datos"));
        
        Club nuevoClub = new Club();
        nuevoClub.setNombreClub("Club sin reclamar - " + emailUsuario);
        nuevoClub.setDescripcion("Este club fue creado por el administrador y está esperando ser reclamado por el usuario.");
        nuevoClub.setRepresentante(usuario);
        nuevoClub.setEstado(estadoSinReclamar);
        nuevoClub.setFechaCreacion(LocalDateTime.now());
        nuevoClub.setAceptaSolicitudes(false);
        nuevoClub.setLimiteCompetidores(Constants.MAX_COMPETIDORES_POR_CLUB);
        nuevoClub.setEstadoActividad(Club.EstadoActividad.ACTIVO);
        nuevoClub.setCambiosPendientesValidacion(false);
        
        Club clubGuardado = clubRepository.save(nuevoClub);
        
        notificacionService.crearNotificacion(
            usuario.getIdUsuario(),
            Constants.NOTIF_CLUB_DISPONIBLE,
            "Tienes un club disponible para reclamar",
            "El administrador ha creado un club para ti. Reclámalo y completa los datos para comenzar."
        );
        
        return convertirAResponse(clubGuardado);
    }

    /**
     * Crear club para usuario autenticado
     */
    @Transactional
    public ClubResponse crearClubParaUsuario(Integer idUsuario, String nombreClub, String descripcion) {
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        List<Club> clubesExistentes = clubRepository.findByRepresentante_IdUsuario(idUsuario);
        
        for (Club clubExistente : clubesExistentes) {
            String estadoDesc = clubExistente.getEstado().getDescripcion();
            
            if ("Sin Reclamar".equals(estadoDesc)) {
                throw new BusinessException("Ya tienes un club sin reclamar. Por favor reclámalo antes de crear otro.");
            }
            if ("Pendiente".equals(estadoDesc)) {
                throw new BusinessException("Ya tienes un club pendiente de validación. Solo se permite un club por usuario.");
            }
            if ("Validado".equals(estadoDesc)) {
                throw new BusinessException("Ya tienes un club validado. Solo se permite un club por usuario.");
            }
            
            if ("Rechazado".equals(estadoDesc)) {
                throw new BusinessException("Ya has creado un club anteriormente (fue rechazado). Solo se permite un club por usuario.");
            }
        }
        
        if (nombreClub.length() > Constants.MAX_NOMBRE_CLUB) {
            throw new BusinessException("El nombre del club no puede exceder " + Constants.MAX_NOMBRE_CLUB + " caracteres");
        }
        
        Optional<Club> clubExistenteOpt = clubRepository.findByNombreClub(nombreClub);
        if (clubExistenteOpt.isPresent()) {
            throw new BusinessException("Ya existe un club con el nombre '" + nombreClub + "'. Por favor elige otro nombre.");
        }
        
        if (descripcion != null && descripcion.length() > Constants.MAX_DESCRIPCION_CLUB) {
            throw new BusinessException("La descripción no puede exceder " + Constants.MAX_DESCRIPCION_CLUB + " caracteres");
        }
        
        CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pendiente' no encontrado en la base de datos"));
        
        Club nuevoClub = new Club();
        nuevoClub.setNombreClub(nombreClub);
        nuevoClub.setDescripcion(descripcion);
        nuevoClub.setRepresentante(usuario);
        nuevoClub.setEstado(estadoPendiente);
        nuevoClub.setFechaCreacion(LocalDateTime.now());
        nuevoClub.setAceptaSolicitudes(false);
        nuevoClub.setLimiteCompetidores(Constants.MAX_COMPETIDORES_POR_CLUB);
        nuevoClub.setEstadoActividad(Club.EstadoActividad.ACTIVO);
        nuevoClub.setCambiosPendientesValidacion(false);
        
        Club clubGuardado = clubRepository.save(nuevoClub);
        
        List<Usuario> administradores = usuarioRepository.findByRolId(Constants.ROL_ADMINISTRADOR);
        
        for (Usuario admin : administradores) {
            notificacionService.crearNotificacion(
                admin.getIdUsuario(),
                Constants.NOTIF_CLUB_PENDIENTE,
                "Nuevo club pendiente de validación",
                "El club '" + nombreClub + "' ha sido creado por " + 
                usuario.getNombreCompleto() + " y está esperando tu validación."
            );
        }
        
        return convertirAResponse(clubGuardado);
    }
    
    /**
     * Reclamar club creado por admin
     */
     @Transactional
     public ClubResponse reclamarClub(Integer clubId, Integer idUsuario, String nombreClub, String descripcion) {
         
         Club club = clubRepository.findById(clubId)
                 .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
         
         if (!club.getEstado().getIdEstado().equals(Constants.ESTADO_SIN_RECLAMAR)) {
             throw new BusinessException("Este club ya fue reclamado");
         }
         
         if (!club.getRepresentante().getIdUsuario().equals(idUsuario)) {
             throw new BusinessException("No tienes permiso para reclamar este club");
         }
         
         if (nombreClub == null || nombreClub.trim().isEmpty()) {
             throw new BusinessException("El nombre del club es obligatorio");
         }
         
         if (nombreClub.length() > Constants.MAX_NOMBRE_CLUB) {
             throw new BusinessException("El nombre del club no puede exceder " + Constants.MAX_NOMBRE_CLUB + " caracteres");
         }
         
         Optional<Club> clubExistenteOpt = clubRepository.findByNombreClub(nombreClub);
         
         if (clubExistenteOpt.isPresent() && !clubExistenteOpt.get().getIdClub().equals(clubId)) {
             throw new BusinessException("Ya existe un club con el nombre '" + nombreClub + "'");
         }
         
         if (descripcion != null && descripcion.length() > Constants.MAX_DESCRIPCION_CLUB) {
             throw new BusinessException("La descripción no puede exceder " + Constants.MAX_DESCRIPCION_CLUB + " caracteres");
         }
         
         club.setNombreClub(nombreClub);
         club.setDescripcion(descripcion);
         
         CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
                 .orElseThrow(() -> new ResourceNotFoundException("Estado 'Pendiente' no encontrado"));
         
         club.setEstado(estadoPendiente);
         
         Club clubActualizado = clubRepository.save(club);
         
         List<Usuario> administradores = usuarioRepository.findByRolId(Constants.ROL_ADMINISTRADOR);
         
         for (Usuario admin : administradores) {
             notificacionService.crearNotificacion(
                 admin.getIdUsuario(),
                 Constants.NOTIF_CLUB_PENDIENTE,
                 "Nuevo club pendiente de validación",
                 "El club '" + nombreClub + "' ha sido reclamado por " + 
                 club.getRepresentante().getNombreCompleto() + " y está esperando tu validación."
             );
         }
         
         return convertirAResponse(clubActualizado);
     }
    
    /**
     * Obtener clubes pendientes de validación (Admin)
     */
    public List<ClubResponse> obtenerClubesPendientes() {
        List<Club> clubes = clubRepository.findByEstadoId(Constants.ESTADO_PENDIENTE);
        return clubes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener club sin reclamar del usuario
     */
    public ClubResponse obtenerClubSinReclamar(Integer idUsuario) {
        List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(idUsuario);
        
        Club clubSinReclamar = clubes.stream()
                .filter(c -> c.getEstado().getIdEstado().equals(Constants.ESTADO_SIN_RECLAMAR))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No tienes ningún club sin reclamar"));
        
        return convertirAResponse(clubSinReclamar);
    }
    
    /**
     * Obtener TODOS los clubes para estadísticas (Admin)
     */
    public List<ClubResponse> obtenerTodosLosClubes() {
        List<Club> clubes = clubRepository.findAllClubesConEstado();
        return clubes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Validar club - Aprobar o Rechazar (Admin)
     */
    @Transactional
    public ClubResponse validarClub(Integer clubId, ValidarClubRequest request) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getEstado().getIdEstado().equals(Constants.ESTADO_PENDIENTE)) {
            throw new BusinessException("El club ya fue procesado anteriormente");
        }
        
        Usuario representante = club.getRepresentante();
        
        if (request.getAprobar()) {
            
            CatEstado estadoValidado = catEstadoRepository.findById(Constants.ESTADO_VALIDADO)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado Validado no encontrado"));
            
            club.setEstado(estadoValidado);
            club.setFechaValidacion(LocalDateTime.now());
            club.setAceptaSolicitudes(true);
            clubRepository.save(club);
            
            robotService.crearRobotAutomatico(representante.getIdUsuario(), club.getIdClub());
            
            asignarRolSiNoTiene(representante.getIdUsuario(), Constants.ROL_REPRESENTANTE);
            asignarRolSiNoTiene(representante.getIdUsuario(), Constants.ROL_COMPETIDOR);
            
            notificacionService.crearNotificacion(
                representante.getIdUsuario(),
                Constants.NOTIF_CLUB_VALIDADO,
                "¡Club validado!",
                "¡Felicidades! Tu club '" + club.getNombreClub() + 
                "' ha sido validado. Ahora tienes permisos de Representante y tu club está activo."
            );
            
        } else {
        	
            if (request.getMotivo() == null || request.getMotivo().trim().isEmpty()) {
                throw new BusinessException("Debes proporcionar un motivo para rechazar el club");
            }
            
            CatEstado estadoRechazado = catEstadoRepository.findById(Constants.ESTADO_RECHAZADO)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado Rechazado no encontrado"));
            
            club.setEstado(estadoRechazado);
            club.setMotivoRechazo(request.getMotivo());
            clubRepository.save(club);
            
            notificacionService.crearNotificacion(
                representante.getIdUsuario(),
                Constants.NOTIF_CLUB_RECHAZADO,
                "Club rechazado",
                "Tu club '" + club.getNombreClub() + "' fue rechazado. Motivo: " + request.getMotivo()
            );
        }
        
        return convertirAResponse(club);
    }
    
    /**
     * Obtener club del representante
     */
    public ClubResponse obtenerMiClub(Integer idRepresentante) {
        List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(idRepresentante);
        
        if (clubes.isEmpty()) {
            throw new ResourceNotFoundException("No tienes ningún club registrado");
        }
        
        Club club = clubes.stream()
                .filter(c -> c.getEstado().getIdEstado().equals(Constants.ESTADO_VALIDADO))
                .findFirst()
                .orElse(clubes.get(0));
        
        return convertirAResponseConMiembros(club);
    }
    
    /**
     * Reenviar club rechazado para nueva validación
     */
    @Transactional
    public ClubResponse reenviarClubParaValidacion(Integer idClub, Integer idUsuario, ReenviarClubRequest request) {
        // Buscar el club
        Club club = clubRepository.findById(idClub)
            .orElseThrow(() -> new ResourceNotFoundException("Club", "id", idClub));
        
        if (!club.getRepresentante().getIdUsuario().equals(idUsuario)) {
            throw new IllegalStateException("Solo el representante del club puede reenviar la solicitud");
        }
        
        if (!club.getEstado().getIdEstado().equals(Constants.ESTADO_RECHAZADO)) {
            throw new IllegalStateException("Solo se pueden reenviar clubes rechazados");
        }
        
        if (!club.getNombreClub().equals(request.getNombreClub())) {
            if (clubRepository.existsByNombreClub(request.getNombreClub())) {
                throw new IllegalStateException("Ya existe un club con ese nombre");
            }
        }
        
        club.setNombreClub(request.getNombreClub());
        club.setDescripcion(request.getDescripcion());
        
        CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
            .orElseThrow(() -> new ResourceNotFoundException("Estado", "id", Constants.ESTADO_PENDIENTE));
        club.setEstado(estadoPendiente);
        
        club.setMotivoRechazo(null);
        
        club = clubRepository.save(club);
        
        notificarAdministradoresClubReenviado(club);
        
        notificarRepresentanteReenvioExitoso(club);
        
        return convertirAResponse(club);
    }

    /**
     * Notifica a todos los administradores que un club rechazado fue reenviado
     */
    private void notificarAdministradoresClubReenviado(Club club) {
        List<Usuario> administradores = usuarioRepository.findByRolId(Constants.ROL_ADMINISTRADOR);
        
        for (Usuario admin : administradores) {
            notificacionService.crearNotificacion(
                admin.getIdUsuario(),
                Constants.NOTIF_CLUB_PENDIENTE,
                "Club Reenviado para Validación",
                String.format(
                    "El club '%s' (previamente rechazado) ha sido actualizado y reenviado para validación.",
                    club.getNombreClub()
                )
            );
        }
    }

    /**
     * Notifica al representante que su club fue reenviado exitosamente
     */
    private void notificarRepresentanteReenvioExitoso(Club club) {
        notificacionService.crearNotificacion(
            club.getRepresentante().getIdUsuario(),
            Constants.NOTIF_CLUB_PENDIENTE,
            "Club Reenviado Exitosamente",
            String.format(
                "Tu club '%s' ha sido reenviado para validación. Los administradores lo revisarán nuevamente.",
                club.getNombreClub()
            )
        );
    }
    
    /**
     * Editar club (Representante)
     */
    @Transactional
    public ClubResponse editarClub(Integer clubId, Integer idRepresentante, String descripcion, String logoUrl) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para editar este club");
        }
        
        if (descripcion != null && descripcion.length() > Constants.MAX_DESCRIPCION_CLUB) {
            throw new BusinessException("La descripción no puede exceder " + Constants.MAX_DESCRIPCION_CLUB + " caracteres");
        }
        
        if (descripcion != null) {
            club.setDescripcion(descripcion);
        }
        if (logoUrl != null) {
            club.setLogoUrl(logoUrl);
        }
        
        clubRepository.save(club);
        
        return convertirAResponse(club);
    }
    
    /**
     * Eliminar club (Representante)
     */
    @Transactional
    public void eliminarClub(Integer clubId, Integer idRepresentante) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para eliminar este club");
        }
        
        if (torneoRepository.existeTorneoEnCurso(Constants.ESTADO_EN_CURSO)) {
            throw new BusinessException("No puedes eliminar el club mientras hay un torneo en curso");
        }
        
        CatEstado estadoEliminado = catEstadoRepository.findById(Constants.ESTADO_ELIMINADO)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Eliminado no encontrado"));
        
        club.setEstado(estadoEliminado);
        clubRepository.save(club);
        
        List<Robot> robots = robotRepository.findRobotsActivosByClubId(clubId);
        
        for (Robot robot : robots) {
            notificacionService.crearNotificacion(
                robot.getUsuario().getIdUsuario(),
                Constants.NOTIF_EXPULSADO_CLUB,
                "Club eliminado",
                "El club '" + club.getNombreClub() + "' ha sido eliminado. Has quedado libre para unirte a otro club."
            );
        }
    }
    
    /**
     * Reactivar club (Representante)
     */
    @Transactional
    public ClubResponse reactivarClub(Integer clubId, Integer idRepresentante) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para reactivar este club");
        }
        
        if (!club.getEstadoActividad().equals(Club.EstadoActividad.INACTIVO)) {
            throw new BusinessException("El club no está marcado como inactivo");
        }
        
        club.setEstadoActividad(Club.EstadoActividad.ACTIVO);
        club.setAceptaSolicitudes(true);
        
        Usuario representante = club.getRepresentante();
        representante.setUltimoInicioSesion(LocalDateTime.now());
        usuarioRepository.save(representante);
        
        clubRepository.save(club);
        
        List<Robot> robots = robotRepository.findRobotsActivosByClubId(clubId);
        
        for (Robot robot : robots) {
            notificacionService.crearNotificacion(
                robot.getUsuario().getIdUsuario(),
                Constants.NOTIF_CLUB_REACTIVADO,
                "Club reactivado",
                "El club '" + club.getNombreClub() + "' ha sido reactivado por el representante."
            );
        }
        
        return convertirAResponse(club);
    }
    
    /**
     * Obtener directorio público de clubes
     */
    public List<ClubSimpleResponse> obtenerClubesPublicos(String search, String ordenar) {
        List<Club> clubes;
        
        if (search != null && !search.trim().isEmpty()) {
            clubes = clubRepository.searchClubesPublicos(Constants.ESTADO_VALIDADO, search);
        } else {
            clubes = clubRepository.findClubesPublicos(Constants.ESTADO_VALIDADO);
        }
        
        if ("competidores".equals(ordenar)) {
            clubes.sort((c1, c2) -> {
                long count1 = robotRepository.countRobotsActivosByClubId(c1.getIdClub());
                long count2 = robotRepository.countRobotsActivosByClubId(c2.getIdClub());
                return Long.compare(count2, count1);
            });
        } else if ("fecha".equals(ordenar)) {
            clubes.sort((c1, c2) -> c2.getFechaCreacion().compareTo(c1.getFechaCreacion()));
        }
        
        return clubes.stream()
                .map(this::convertirASimpleResponse)
                .collect(Collectors.toList());
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
     * Obtener historial detallado de un club en una categoría
     */
    public ClubHistorialResponse obtenerHistorialClub(Integer clubId, Integer categoriaId) {
        
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        List<PuntosClubHistorico> historial = puntosClubHistoricoRepository
                .getHistorialClubPorCategoria(clubId, categoriaId);
        
        if (historial.isEmpty()) {
            throw new ResourceNotFoundException("No hay historial para esta categoría en el club");
        }
        
        List<ClubHistorialResponse.MiembroHistoricoResponse> miembros = historial.stream()
                .map(h -> {
                	
                    Robot robot = robotRepository.findByUsuario_IdUsuario(h.getUsuario().getIdUsuario())
                            .orElse(null);
                    
                    String nombreRobot = robot != null ? robot.getNombreRobot() : "Sin robot";
                    
                    String periodo = formatearPeriodo(h.getFechaInicio(), h.getFechaFin());
                    
                    return new ClubHistorialResponse.MiembroHistoricoResponse(
                        h.getUsuario().getNombreCompleto(),
                        nombreRobot,
                        h.getPuntosGanadosAqui(),
                        h.getFechaInicio(),
                        h.getFechaFin(),
                        h.isActivo(),
                        periodo
                    );
                })
                .collect(Collectors.toList());
        
        Integer puntosHistoricos = puntosClubHistoricoRepository
                .calcularPuntosTotalesClub(clubId, categoriaId);
        
        Long competidoresActivos = puntosClubHistoricoRepository
                .contarCompetidoresActivosPorClub(clubId, categoriaId);
        
        String nombreCategoria = historial.get(0).getCategoria().getNombreCategoria();
        
        return new ClubHistorialResponse(
            club.getIdClub(),
            club.getNombreClub(),
            nombreCategoria,
            puntosHistoricos,
            competidoresActivos.intValue(),
            miembros
        );
    }
    
    /**
     * Formatear periodo de tiempo
     */
    private String formatearPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        String inicioStr = inicio.format(formatter);
        
        if (fin == null) {
            return inicioStr + " - Actualidad";
        } else {
            String finStr = fin.format(formatter);
            return inicioStr + " - " + finStr;
        }
    }
    
    /**
     * Convertir entidad a DTO completo
     */
    public ClubResponse convertirAResponse(Club club) {
        long competidoresActivos = robotRepository.countRobotsActivosByClubId(club.getIdClub());
        
        return new ClubResponse(
            club.getIdClub(),
            club.getNombreClub(),
            club.getDescripcion(),
            club.getLogoUrl(),
            club.getRepresentante().getNombreCompleto(),
            club.getRepresentante().getEmail(),
            club.getRepresentante().getIdUsuario(),
            club.getEstado().getDescripcion(),
            club.getFechaCreacion(),
            club.getFechaValidacion(),
            club.getMotivoRechazo(),
            club.getAceptaSolicitudes(),
            club.getLimiteCompetidores(),
            (int) competidoresActivos,
            club.getEstadoActividad().name()
        );
    }
    
    /**
     * Convertir entidad a DTO simple
     */
    private ClubSimpleResponse convertirASimpleResponse(Club club) {
        long competidoresActivos = robotRepository.countRobotsActivosByClubId(club.getIdClub());
        
        return new ClubSimpleResponse(
            club.getIdClub(),
            club.getNombreClub(),
            club.getDescripcion(),
            club.getLogoUrl(),
            (int) competidoresActivos,
            club.getAceptaSolicitudes()
        );
    }    
    /**
     * Convertir entidad a DTO completo CON lista de miembros
     */
    public ClubResponse convertirAResponseConMiembros(Club club) {
        long competidoresActivos = robotRepository.countRobotsActivosByClubId(club.getIdClub());
        
        List<Robot> robots = robotRepository.findRobotsActivosByClubId(club.getIdClub());

        List<ClubResponse.MiembroDTO> miembros = robots.stream()
            .map(robot -> new ClubResponse.MiembroDTO(
                robot.getUsuario().getIdUsuario(),
                robot.getUsuario().getNombreCompleto(),
                robot.getUsuario().getEmail(),
                robot.getIdRobot(),
                robot.getNombreRobot()
            ))
            .collect(Collectors.toList());
        
        ClubResponse response = new ClubResponse(
            club.getIdClub(),
            club.getNombreClub(),
            club.getDescripcion(),
            club.getLogoUrl(),
            club.getRepresentante().getNombreCompleto(),
            club.getRepresentante().getEmail(),
            club.getRepresentante().getIdUsuario(),
            club.getEstado().getDescripcion(),
            club.getFechaCreacion(),
            club.getFechaValidacion(),
            club.getMotivoRechazo(),
            club.getAceptaSolicitudes(),
            club.getLimiteCompetidores(),
            (int) competidoresActivos,
            club.getEstadoActividad().name()
        );
        
        response.setMiembros(miembros);
        
        return response;
    }
}