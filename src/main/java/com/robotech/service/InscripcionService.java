package com.robotech.service;

import com.robotech.dto.InscribirClubRequest;
import com.robotech.dto.InscripcionRequest;
import com.robotech.dto.InscripcionResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.*;
import com.robotech.repository.*;
import com.robotech.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InscripcionService {
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private PuntosClubHistoricoRepository puntosClubHistoricoRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    /**
     * Inscribir club completo en torneo (Representante)
     */
    @Transactional
    public void inscribirClub(InscribirClubRequest request, Integer idRepresentante) {
    	
        Torneo torneo = torneoRepository.findById(request.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", request.getIdTorneo()));
        
        if (!torneo.getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
            throw new BusinessException("El torneo ya inició o finalizó. No se pueden agregar inscripciones.");
        }
        
        List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(idRepresentante);
        if (clubes.isEmpty()) {
            throw new BusinessException("No tienes un club registrado");
        }
        
        Club club = clubes.stream()
                .filter(c -> c.getEstado().getIdEstado().equals(Constants.ESTADO_VALIDADO))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tu club no está validado"));
        
        if (club.getEstadoActividad() != Club.EstadoActividad.ACTIVO) {
            throw new BusinessException("Tu club debe estar activo para inscribirse en torneos");
        }
        
        List<String> errores = new ArrayList<>();
        int totalInscripciones = 0;
        
        for (InscribirClubRequest.InscripcionCategoriaDto categoriaDto : request.getCategorias()) {
            Categoria categoria = categoriaRepository.findById(categoriaDto.getIdCategoria())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaDto.getIdCategoria()));
            
            if (!categoria.getTorneo().getIdTorneo().equals(request.getIdTorneo())) {
                errores.add("La categoría '" + categoria.getNombreCategoria() + "' no pertenece a este torneo");
                continue;
            }
            
            if (!categoria.getEstado().getIdEstado().equals(Constants.ESTADO_ACTIVA)) {
                errores.add("La categoría '" + categoria.getNombreCategoria() + "' no está disponible para inscripciones");
                continue;
            }
            
            long inscritosActuales = inscripcionRepository.countByCategoria(categoria.getIdCategoria());
            if (inscritosActuales + categoriaDto.getIdsCompetidores().size() > categoria.getCupoMaximo()) {
                errores.add("La categoría '" + categoria.getNombreCategoria() + "' ha alcanzado su cupo máximo");
                continue;
            }
            
            for (Integer idCompetidor : categoriaDto.getIdsCompetidores()) {
                try {
                    inscribirCompetidor(idCompetidor, categoria, club, idRepresentante);
                    totalInscripciones++;
                } catch (BusinessException e) {
                    errores.add(e.getMessage());
                }
            }
        }
        
        if (!errores.isEmpty() && totalInscripciones > 0) {
            throw new BusinessException(
                "Se inscribieron " + totalInscripciones + " competidores, pero hubo errores: " + 
                String.join(", ", errores)
            );
        }
        
        if (totalInscripciones == 0 && !errores.isEmpty()) {
            throw new BusinessException("No se pudo inscribir a ningún competidor: " + String.join(", ", errores));
        }
        
        if (totalInscripciones > 0) {
        	
            for (InscribirClubRequest.InscripcionCategoriaDto categoriaDto : request.getCategorias()) {
                Categoria categoria = categoriaRepository.findById(categoriaDto.getIdCategoria()).orElse(null);
                if (categoria != null) {
                    for (Integer idCompetidor : categoriaDto.getIdsCompetidores()) {
                        notificacionService.crearNotificacion(
                            idCompetidor,
                            Constants.NOTIF_INSCRIPCION_TORNEO,
                            "Inscrito en torneo",
                            "Has sido inscrito en la categoría '" + categoria.getNombreCategoria() + 
                            "' del torneo '" + torneo.getNombreTorneo() + "' por tu representante."
                        );
                    }
                }
            }
        }
    }
    
    /**
     * Solicitar inscripción individual (Competidor)
     */
    @Transactional
    public InscripcionResponse solicitarInscripcionIndividual(Integer idUsuario, InscripcionRequest request) {
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new BusinessException("No tienes un robot creado"));
        
        if (!robot.getEstado().equals(Robot.EstadoRobot.ACTIVO)) {
            throw new BusinessException("Tu robot debe estar activo para inscribirse");
        }
        
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getIdCategoria()));
        
        Torneo torneo = categoria.getTorneo();
        
        if (!torneo.getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
            throw new BusinessException("El torneo no está aceptando inscripciones");
        }
        
        if (categoria.getCategoriaPeso() != null && robot.getCategoriaPeso() != null) {
            if (!categoria.getCategoriaPeso().getIdCategoriaPeso().equals(robot.getCategoriaPeso().getIdCategoriaPeso())) {
                throw new BusinessException(
                    "Tu robot no cumple con los requisitos de peso de esta categoría. " +
                    "Tu robot es " + robot.getCategoriaPeso().getNombre() + 
                    " pero la categoría requiere " + categoria.getCategoriaPeso().getNombre()
                );
            }
        }
        
        boolean yaInscrito = inscripcionRepository.existsByUsuario_IdUsuarioAndCategoria_IdCategoria(
            idUsuario, request.getIdCategoria()
        );
        if (yaInscrito) {
            throw new BusinessException("Ya estás inscrito en esta categoría");
        }
        
        long inscritosActuales = inscripcionRepository.countByCategoria_IdCategoria(request.getIdCategoria());
        if (inscritosActuales >= categoria.getCupoMaximo()) {
            throw new BusinessException("La categoría ha alcanzado el cupo máximo");
        }
        
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuario(usuario);
        inscripcion.setRobot(robot);
        inscripcion.setCategoria(categoria);
        inscripcion.setTorneo(torneo);
        inscripcion.setEstado(Inscripcion.ESTADO_PENDIENTE);
        inscripcion.setPuntajeAcumulado(0);
        
        CatEstado estadoActiva = catEstadoRepository.findById(Constants.ESTADO_ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Activa no encontrado"));
        inscripcion.setEstadoCategoria(estadoActiva);
        
        inscripcion = inscripcionRepository.save(inscripcion);
        
        Robot robotUsuario = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElse(null);
        Club club = (robotUsuario != null) ? robotUsuario.getClub() : null;
        
        if (club != null && club.getRepresentante() != null) {
            notificacionService.crearNotificacion(
                club.getRepresentante().getIdUsuario(),
                "SOLICITUD_INSCRIPCION",
                usuario.getNombreCompleto() + " solicita inscribirse al torneo " + torneo.getNombreTorneo(),
                "/representante/inscripciones-pendientes"
            );
        }
        
        return convertirInscripcionAResponse(inscripcion);
    }
    
    /**
     * Obtener mis inscripciones (Competidor)
     */
    @Transactional(readOnly = true)
    public List<InscripcionResponse> obtenerMisInscripciones(Integer idUsuario) {
        List<Inscripcion> inscripciones = inscripcionRepository.findByUsuario_IdUsuario(idUsuario);
        
        return inscripciones.stream()
                .map(this::convertirInscripcionAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener inscripciones pendientes del club (Representante)
     */
    @Transactional(readOnly = true)
    public List<InscripcionResponse> obtenerInscripcionesPendientes(Integer clubId) {
        List<Inscripcion> pendientes = inscripcionRepository.findInscripcionesPendientesPorClub(clubId);
        
        return pendientes.stream()
                .map(this::convertirInscripcionAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Aprobar inscripción individual (Representante)
     */
    @Transactional
    public void aprobarInscripcion(Integer idInscripcion, Integer idRepresentante) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", "id", idInscripcion));
        
        if (!Inscripcion.ESTADO_PENDIENTE.equals(inscripcion.getEstado())) {
            throw new BusinessException("La inscripción ya fue procesada");
        }
        
        Robot robot = robotRepository.findByUsuario_IdUsuario(inscripcion.getUsuario().getIdUsuario())
                .orElseThrow(() -> new BusinessException("El usuario no tiene robot"));
        
        if (robot.getClub() == null || 
            !robot.getClub().getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para aprobar esta inscripción");
        }
        
        inscripcion.setEstado(Inscripcion.ESTADO_APROBADA);
        inscripcion.setFechaAprobacion(LocalDateTime.now());
        inscripcionRepository.save(inscripcion);
        
        crearRegistroHistorico(inscripcion.getUsuario(), robot.getClub(), inscripcion.getCategoria());
        
        notificacionService.crearNotificacion(
            inscripcion.getUsuario().getIdUsuario(),
            Constants.NOTIF_SOLICITUD_ACEPTADA,
            "Inscripción aprobada",
            "Tu inscripción en '" + inscripcion.getCategoria().getNombreCategoria() + 
            "' fue aprobada por tu representante"
        );
    }
    
    /**
     * Rechazar inscripción individual (Representante)
     */
    @Transactional
    public void rechazarInscripcion(Integer idInscripcion, Integer idRepresentante) {
        Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", "id", idInscripcion));
        
        if (!Inscripcion.ESTADO_PENDIENTE.equals(inscripcion.getEstado())) {
            throw new BusinessException("La inscripción ya fue procesada");
        }
        
        Robot robot = robotRepository.findByUsuario_IdUsuario(inscripcion.getUsuario().getIdUsuario())
                .orElseThrow(() -> new BusinessException("El usuario no tiene robot"));
        
        if (robot.getClub() == null || 
            !robot.getClub().getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para rechazar esta inscripción");
        }
        
        inscripcion.setEstado(Inscripcion.ESTADO_RECHAZADA);
        inscripcionRepository.save(inscripcion);
        
        notificacionService.crearNotificacion(
            inscripcion.getUsuario().getIdUsuario(),
            Constants.NOTIF_SOLICITUD_RECHAZADA,
            "Inscripción rechazada",
            "Tu inscripción en '" + inscripcion.getCategoria().getNombreCategoria() + 
            "' fue rechazada por tu representante"
        );
    }
    
    /**
     * Inscribir un competidor en una categoría (método privado)
     */
    private void inscribirCompetidor(Integer idCompetidor, Categoria categoria, Club club, Integer idRepresentante) {
        Usuario competidor = usuarioRepository.findById(idCompetidor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idCompetidor));
        
        Optional<Robot> robotOpt = robotRepository.findByUsuario_IdUsuario(idCompetidor);
        if (robotOpt.isEmpty()) {
            throw new BusinessException(competidor.getNombreCompleto() + " no tiene robot creado");
        }
        
        Robot robot = robotOpt.get();
        
        if (robot.getClub() == null || !robot.getClub().getIdClub().equals(club.getIdClub())) {
            throw new BusinessException(competidor.getNombreCompleto() + " no pertenece a tu club");
        }
        
        if (robot.getEstado() != Robot.EstadoRobot.ACTIVO) {
            throw new BusinessException(competidor.getNombreCompleto() + " no tiene robot activo");
        }
        
        if (inscripcionRepository.existsByUsuarioAndCategoria(idCompetidor, categoria.getIdCategoria())) {
            throw new BusinessException(competidor.getNombreCompleto() + " ya está inscrito en esta categoría");
        }
        
        CatEstado estadoActiva = catEstadoRepository.findById(Constants.ESTADO_ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Activa no encontrado"));
        
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuario(competidor);
        inscripcion.setRobot(robot);
        inscripcion.setCategoria(categoria);
        inscripcion.setTorneo(categoria.getTorneo());
        inscripcion.setPuntajeAcumulado(0);
        inscripcion.setEstado(Inscripcion.ESTADO_APROBADA);
        inscripcion.setEstadoCategoria(estadoActiva);
        inscripcionRepository.save(inscripcion);
        
        crearRegistroHistorico(competidor, club, categoria);
    }
    
    /**
     * Crear registro histórico cuando se inscribe un competidor
     */
    private void crearRegistroHistorico(Usuario competidor, Club club, Categoria categoria) {
    	
        Optional<PuntosClubHistorico> registroExistente = 
            puntosClubHistoricoRepository.findActivoByUsuarioAndClubAndCategoria(
                competidor.getIdUsuario(), 
                club.getIdClub(), 
                categoria.getIdCategoria()
            );
        
        if (registroExistente.isEmpty()) {
            
            PuntosClubHistorico historico = PuntosClubHistorico.builder()
                    .club(club)
                    .usuario(competidor)
                    .categoria(categoria)
                    .puntosGanadosAqui(0)
                    .fechaInicio(LocalDateTime.now())
                    .fechaFin(null)
                    .build();
            
            puntosClubHistoricoRepository.save(historico);
        }
    }
    
    /**
     * Convertir inscripción a DTO
     */
    private InscripcionResponse convertirInscripcionAResponse(Inscripcion inscripcion) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(inscripcion.getUsuario().getIdUsuario())
                .orElse(null);
        
        String nombreRobot = robot != null ? robot.getNombreRobot() : "Sin robot";
        
        LocalDateTime fechaAprobacion = null;
        if (Inscripcion.ESTADO_APROBADA.equals(inscripcion.getEstado())) {
            fechaAprobacion = null;
        }
        
        return new InscripcionResponse(
            inscripcion.getIdInscripcion(),
            inscripcion.getUsuario().getNombreCompleto(),
            nombreRobot,
            inscripcion.getCategoria().getTorneo().getNombreTorneo(),
            inscripcion.getCategoria().getNombreCategoria(),
            inscripcion.getEstado(),
            inscripcion.getFechaSolicitud(),
            fechaAprobacion,
            null
        );
    }
    
    /**
     * Obtener inscripciones de una categoría
     */
    public List<Inscripcion> obtenerInscripcionesPorCategoria(Integer categoriaId) {
        return inscripcionRepository.findByCategoria_IdCategoria(categoriaId);
    }
    
    /**
     * Verificar si un usuario está inscrito en una categoría
     */
    public boolean estaInscrito(Integer idUsuario, Integer idCategoria) {
        return inscripcionRepository.existsByUsuarioAndCategoria(idUsuario, idCategoria);
    }
    
    /**
     * Obtener inscripción específica
     */
    public Optional<Inscripcion> obtenerInscripcion(Integer idUsuario, Integer idCategoria) {
        return inscripcionRepository.findByUsuario_IdUsuarioAndCategoria_IdCategoria(idUsuario, idCategoria);
    }
}