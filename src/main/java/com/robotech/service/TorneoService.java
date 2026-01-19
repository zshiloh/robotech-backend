package com.robotech.service;

import com.robotech.dto.TorneoRequest;
import com.robotech.dto.TorneoResponse;
import com.robotech.dto.TorneoResultadoResponse;
import com.robotech.dto.TorneoResultadoResponse.CategoriaResultadoResponse;
import com.robotech.dto.TorneoResultadoResponse.ParticipanteResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TorneoService {
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private SedeRepository sedeRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private EnfrentamientoService enfrentamientoService;
    
    @Autowired
    private RankingRepository rankingRepository;
    
    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    /**
     * Crear torneo (Organizador)
     */
    @Transactional
    public TorneoResponse crearTorneo(TorneoRequest request) {
    	
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        if (request.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La fecha de inicio debe ser futura");
        }
        
        Sede sede = sedeRepository.findById(request.getIdSede())
                .orElseThrow(() -> new ResourceNotFoundException("Sede", "id", request.getIdSede()));
        
        CatEstado estadoConfiguracion = catEstadoRepository.findById(Constants.ESTADO_CONFIGURACION)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Configuración no encontrado"));
        
        Torneo torneo = new Torneo();
        torneo.setNombreTorneo(request.getNombreTorneo());
        torneo.setDescripcion(request.getDescripcion());
        torneo.setFechaInicio(request.getFechaInicio());
        torneo.setFechaFin(request.getFechaFin());
        torneo.setSede(sede);
        torneo.setEstado(estadoConfiguracion);
        
        torneoRepository.save(torneo);
        
        return convertirAResponse(torneo);
    }
    
    /**
     * Iniciar torneo (Organizador)
     */
    @Transactional
    public TorneoResponse iniciarTorneo(Integer torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", torneoId));
        
        if (!torneo.getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
            throw new BusinessException("El torneo ya fue iniciado o finalizado");
        }
        
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneoId);
        
        if (categorias.isEmpty()) {
            throw new BusinessException("El torneo debe tener al menos una categoría");
        }
        
        for (Categoria categoria : categorias) {
            long inscritos = inscripcionRepository.countByCategoria(categoria.getIdCategoria());
            
            if (inscritos < categoria.getCupoMinimo()) {
                throw new BusinessException(
                    "La categoría '" + categoria.getNombreCategoria() + "' tiene " + inscritos + 
                    " inscritos pero requiere mínimo " + categoria.getCupoMinimo()
                );
            }
            
            if (inscritos % 2 != 0) {
                throw new BusinessException(
                    "La categoría '" + categoria.getNombreCategoria() + "' tiene " + inscritos + 
                    " inscritos. El número de inscritos debe ser PAR para generar los brackets."
                );
            }
        }
        
        CatEstado estadoEnCurso = catEstadoRepository.findById(Constants.ESTADO_EN_CURSO)
                .orElseThrow(() -> new ResourceNotFoundException("Estado En Curso no encontrado"));
        
        torneo.setEstado(estadoEnCurso);
        torneoRepository.save(torneo);
        
        CatEstado estadoCerrada = catEstadoRepository.findById(Constants.ESTADO_CERRADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Cerrada no encontrado"));
        
        for (Categoria categoria : categorias) {
            categoria.setEstado(estadoCerrada);
            categoria.setTieneEnfrentamientos(true);
            categoriaRepository.save(categoria);
            
            enfrentamientoService.generarBrackets(categoria.getIdCategoria());
        }
        
        notificarJuradosTorneoIniciado(torneo);
        
        return convertirAResponse(torneo);
    }
    
    /**
     * Finalizar torneo (Organizador)
     */
    @Transactional
    public TorneoResponse finalizarTorneo(Integer torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", torneoId));
        
        if (!torneo.getEstado().getIdEstado().equals(Constants.ESTADO_EN_CURSO)) {
            throw new BusinessException("El torneo no está en curso");
        }
        
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneoId);
        
        for (Categoria categoria : categorias) {
            long pendientes = enfrentamientoRepository
                .countByCategoria_IdCategoriaAndEstadoCombate(
                    categoria.getIdCategoria(), 
                    "Pendiente"
                );
            
            if (pendientes > 0) {
                throw new BusinessException(
                    "No se puede finalizar el torneo. La categoría '" + 
                    categoria.getNombreCategoria() + "' tiene " + pendientes + 
                    " enfrentamiento(s) pendiente(s)."
                );
            }
        }
        
        CatEstado estadoFinalizado = catEstadoRepository.findById(Constants.ESTADO_FINALIZADO)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Finalizado no encontrado"));
        
        torneo.setEstado(estadoFinalizado);
        torneoRepository.save(torneo);
        
        CatEstado estadoCategoriaFinalizada = catEstadoRepository.findById(Constants.ESTADO_FINALIZADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Finalizada no encontrado"));
        
        for (Categoria categoria : categorias) {
            categoria.setEstado(estadoCategoriaFinalizada);
            categoriaRepository.save(categoria);
        }
        
        notificarCompetidoresTorneoFinalizado(torneo);
        
        return convertirAResponse(torneo);
    }
    
    /**
     * Notificar a todos los jurados cuando un torneo se inicia
     */
    private void notificarJuradosTorneoIniciado(Torneo torneo) {
        List<Usuario> jurados = usuarioRepository.findByRolId(Constants.ROL_JURADO);
        
        for (Usuario jurado : jurados) {
            notificacionService.crearNotificacion(
                jurado.getIdUsuario(),
                Constants.NOTIF_TORNEO_INICIADO,
                "Nuevo torneo iniciado",
                "El torneo '" + torneo.getNombreTorneo() + "' ha iniciado. Hay combates disponibles para arbitrar."
            );
        }
    }
    
    /**
     * Notificar a todos los competidores cuando el torneo finaliza
     */
    private void notificarCompetidoresTorneoFinalizado(Torneo torneo) {
    	
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneo.getIdTorneo());
        
        Set<Integer> idsCompetidores = new HashSet<>();
        
        for (Categoria categoria : categorias) {
            List<Inscripcion> inscripciones = inscripcionRepository
                .findByCategoria_IdCategoria(categoria.getIdCategoria());
            
            for (Inscripcion inscripcion : inscripciones) {
                idsCompetidores.add(inscripcion.getUsuario().getIdUsuario());
            }
        }
        
        for (Integer idCompetidor : idsCompetidores) {
            notificacionService.crearNotificacion(
                idCompetidor,
                Constants.NOTIF_TORNEO_FINALIZADO,
                "Torneo finalizado",
                "El torneo '" + torneo.getNombreTorneo() + 
                "' ha finalizado oficialmente. Ya puedes consultar los resultados finales."
            );
        }
    }
    
    /**
     * Obtener todos los torneos ordenados por fecha
     */
    @Transactional(readOnly = true)
    public List<TorneoResponse> obtenerTodosLosTorneos() {
        List<Torneo> torneos = torneoRepository.findAllOrderByFechaDesc();
        
        return torneos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener torneos finalizados
     */
    public List<TorneoResponse> obtenerTorneosFinalizados() {
        List<Torneo> torneos = torneoRepository.findByEstadoId(Constants.ESTADO_FINALIZADO);
        
        return torneos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener torneos por estado
     */
    public List<TorneoResponse> obtenerTorneosPorEstado(Integer estadoId) {
        List<Torneo> torneos = torneoRepository.findByEstadoId(estadoId);
        
        return torneos.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener torneo por ID
     */
    public TorneoResponse obtenerTorneoPorId(Integer torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", torneoId));
        
        return convertirAResponse(torneo);
    }
    
    /**
     * Obtener resultados completos de un torneo con TODOS los participantes
     */
    public TorneoResultadoResponse obtenerResultadosTorneo(Integer torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", torneoId));
        
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneoId);
        
        List<CategoriaResultadoResponse> resultadosPorCategoria = new ArrayList<>();
        
        for (Categoria categoria : categorias) {
        	
            List<Ranking> todosLosParticipantes = rankingRepository
                    .findByCategoria_IdCategoriaOrderByPosicionAsc(categoria.getIdCategoria());
            
            List<ParticipanteResponse> rankingCompleto = todosLosParticipantes.stream()
                    .map(r -> {
                    	
                        Usuario usuario = r.getInscripcion().getUsuario();
                        
                        Robot robot = robotRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                                .orElse(null);
                        
                        String nombreRobot = "Sin robot";
                        String nombreClub = "Sin club";
                        
                        if (robot != null) {
                            nombreRobot = robot.getNombreRobot();
                            if (robot.getClub() != null) {
                                nombreClub = robot.getClub().getNombreClub();
                            }
                        }
                        
                        return new ParticipanteResponse(
                            r.getPosicion(),
                            usuario.getNombreCompleto(),
                            nombreRobot,
                            nombreClub,
                            r.getVictorias(),
                            r.getDerrotas(),
                            r.getPuntosTotales()
                        );
                    })
                    .collect(Collectors.toList());
            
            List<ParticipanteResponse> podio = rankingCompleto.stream()
                    .limit(3)
                    .collect(Collectors.toList());
            
            String clubGanador = "Sin ganador";
            if (!podio.isEmpty()) {
                clubGanador = podio.get(0).getNombreClub();
            }
            
            long totalParticipantes = inscripcionRepository.countByCategoria(categoria.getIdCategoria());
            long totalEnfrentamientos = enfrentamientoRepository.countByCategoria_IdCategoria(categoria.getIdCategoria());
            
            CategoriaResultadoResponse categoriaResultado = new CategoriaResultadoResponse(
                categoria.getNombreCategoria(),
                podio,
                rankingCompleto,
                (int) totalParticipantes,
                (int) totalEnfrentamientos,
                clubGanador
            );
            
            resultadosPorCategoria.add(categoriaResultado);
        }
        
        return new TorneoResultadoResponse(
            torneo.getIdTorneo(),
            torneo.getNombreTorneo(),
            torneo.getDescripcion(),
            torneo.getFechaInicio(),
            torneo.getFechaFin(),
            torneo.getSede().getNombreSede(),
            torneo.getEstado().getDescripcion(),
            resultadosPorCategoria
        );
    }
    
    /**
     * Verificar si hay torneo en curso
     */
    public boolean hayTorneoEnCurso() {
        return torneoRepository.existeTorneoEnCurso(Constants.ESTADO_EN_CURSO);
    }
    
    /**
     * Convertir entidad a DTO
     */
    private TorneoResponse convertirAResponse(Torneo torneo) {
        long totalCategorias = categoriaRepository.findByTorneo_IdTorneo(torneo.getIdTorneo()).size();
        
        return new TorneoResponse(
            torneo.getIdTorneo(),
            torneo.getNombreTorneo(),
            torneo.getDescripcion(),
            torneo.getFechaInicio(),
            torneo.getFechaFin(),
            torneo.getSede().getNombreSede(),
            torneo.getEstado().getIdEstado(),
            torneo.getEstado().getDescripcion(),
            (int) totalCategorias
        );
    }
}