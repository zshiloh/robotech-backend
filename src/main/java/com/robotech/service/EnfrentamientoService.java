package com.robotech.service;

import com.robotech.dto.EnfrentamientoResponse;
import com.robotech.dto.ResultadoRequest;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnfrentamientoService {
    
    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private CatFaseRepository catFaseRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private RankingService rankingService;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    /**
     * Generar brackets de eliminación simple para una categoría
     */
    @Transactional
    public void generarBrackets(Integer categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
        
        List<Inscripcion> inscripciones = inscripcionRepository.findByCategoria_IdCategoria(categoriaId);
        
        if (inscripciones.isEmpty()) {
            throw new BusinessException("No hay inscritos en esta categoría");
        }
        
        int numCompetidores = inscripciones.size();
        
        if (numCompetidores % 2 != 0) {
            throw new BusinessException("El número de competidores debe ser PAR para generar brackets");
        }
        
        List<Inscripcion> inscripcionesAleatorias = new ArrayList<>(inscripciones);
        Collections.shuffle(inscripcionesAleatorias);
        
        int totalRondas = calcularNumeroDeRondas(numCompetidores);
        CatFase faseInicial = determinarFaseInicial(numCompetidores);
        
        LocalDateTime fechaBase = categoria.getTorneo().getFechaInicio();
        
        generarTodasLasRondas(categoria, inscripcionesAleatorias, faseInicial, totalRondas, fechaBase);
        
        rankingService.recalcularRankingIndividual(categoriaId);
        rankingService.recalcularRankingClub(categoriaId);
    }
    
    /**
     * Generar todas las rondas del torneo (completas y vacías)
     */
    private void generarTodasLasRondas(Categoria categoria, List<Inscripcion> inscripciones, 
                                        CatFase faseInicial, int totalRondas, LocalDateTime fechaBase) {
        
        int idFaseActual = faseInicial.getIdFase();
        
        final int[] offsetHoras = {0};
        
        int numCompetidores = inscripciones.size();
        
        for (int ronda = 0; ronda < totalRondas; ronda++) {
        	
            int enfrentamientosEnEstaRonda = numCompetidores / (int) Math.pow(2, ronda + 1);
            
            final int idFaseActualFinal = idFaseActual;
            CatFase fase = catFaseRepository.findById(idFaseActualFinal)
                    .orElseThrow(() -> new ResourceNotFoundException("Fase", "id", idFaseActualFinal));
            
            if (ronda == 0) {
                for (int i = 0; i < inscripciones.size(); i += 2) {
                    Inscripcion inscripcion1 = inscripciones.get(i);
                    Inscripcion inscripcion2 = inscripciones.get(i + 1);
                    
                    Enfrentamiento enfrentamiento = new Enfrentamiento();
                    enfrentamiento.setCategoria(categoria);
                    enfrentamiento.setFase(fase);
                    enfrentamiento.setCompetidor1(inscripcion1.getUsuario());
                    enfrentamiento.setCompetidor2(inscripcion2.getUsuario());
                    enfrentamiento.setGanador(null);
                    enfrentamiento.setPuntosOtorgados(null);
                    enfrentamiento.setFechaHora(fechaBase.plusHours(offsetHoras[0]++));
                    enfrentamiento.setEstadoCombate("Pendiente");
                    
                    enfrentamientoRepository.save(enfrentamiento);
                }
            }
            
            else {
                for (int i = 0; i < enfrentamientosEnEstaRonda; i++) {
                    Enfrentamiento enfrentamiento = new Enfrentamiento();
                    enfrentamiento.setCategoria(categoria);
                    enfrentamiento.setFase(fase);
                    enfrentamiento.setCompetidor1(null);
                    enfrentamiento.setCompetidor2(null);
                    enfrentamiento.setGanador(null);
                    enfrentamiento.setPuntosOtorgados(null);
                    enfrentamiento.setFechaHora(fechaBase.plusHours(offsetHoras[0]++));
                    enfrentamiento.setEstadoCombate("Pendiente");
                    
                    enfrentamientoRepository.save(enfrentamiento);
                }
            }
            
            idFaseActual++;
        }
    }
    
    /**
     * Calcular número de rondas según competidores
     */
    private int calcularNumeroDeRondas(int numCompetidores) {
        return (int) (Math.log(numCompetidores) / Math.log(2));
    }
    
    /**
     * Avanzar ganador a la siguiente ronda automáticamente
     */
    private void avanzarGanadorASiguienteRonda(Enfrentamiento enfrentamientoActual, Usuario usuarioGanador) {
        CatFase faseActual = enfrentamientoActual.getFase();
        
        if (faseActual.getIdFase() == 7) {
            return;
        }
        
        Integer idSiguienteFase = faseActual.getIdFase() + 1;
        CatFase siguienteFase = catFaseRepository.findById(idSiguienteFase)
                .orElseThrow(() -> new ResourceNotFoundException("Fase siguiente", "id", idSiguienteFase));
        
        List<Enfrentamiento> enfrentamientosSiguienteFase = enfrentamientoRepository
                .findByCategoria_IdCategoriaAndFase_IdFaseOrderByIdEnfrentamientoAsc(
                    enfrentamientoActual.getCategoria().getIdCategoria(), 
                    idSiguienteFase
                );
        
        for (Enfrentamiento siguiente : enfrentamientosSiguienteFase) {
            if (siguiente.getCompetidor1() == null) {
            	siguiente.setCompetidor1(usuarioGanador);
                enfrentamientoRepository.save(siguiente);
                return;
            } else if (siguiente.getCompetidor2() == null) {
            	siguiente.setCompetidor2(usuarioGanador);
                enfrentamientoRepository.save(siguiente);
                return;
            }
        }
    }
    
    /**
     * Registrar resultado de un enfrentamiento (Jurado)
     */
    @Transactional
    public void registrarResultado(Integer enfrentamientoId, Integer idJurado, ResultadoRequest request) {
        Enfrentamiento enfrentamiento = enfrentamientoRepository.findById(enfrentamientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Enfrentamiento", "id", enfrentamientoId));
        
        if (!"Pendiente".equals(enfrentamiento.getEstadoCombate())) {
            throw new BusinessException("Este enfrentamiento ya tiene resultado registrado");
        }
        
        Usuario usuario1 = enfrentamiento.getCompetidor1();
        Usuario usuario2 = enfrentamiento.getCompetidor2();
        
        if (usuario1 == null || usuario2 == null) {
            throw new BusinessException("Este enfrentamiento aún no tiene ambos competidores asignados");
        }

        Integer idUsuario1 = usuario1.getIdUsuario();
        Integer idUsuario2 = usuario2.getIdUsuario();
        Integer idGanador = request.getIdGanador();
        
        if (!idGanador.equals(idUsuario1) && !idGanador.equals(idUsuario2)) {
            throw new BusinessException("El ganador debe ser uno de los competidores del enfrentamiento");
        }
        
        if (request.getPuntosOtorgados() < 0) {
            throw new BusinessException("Los puntos no pueden ser negativos");
        }
        
        Usuario jurado = usuarioRepository.findById(idJurado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idJurado));
        
        Usuario ganador = usuarioRepository.findById(idGanador)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idGanador));
        
        Usuario usuarioGanador = idGanador.equals(idUsuario1) ? usuario1 : usuario2;
        
        enfrentamiento.setGanador(ganador);
        enfrentamiento.setPuntosOtorgados(request.getPuntosOtorgados());
        enfrentamiento.setEstadoCombate("Finalizado");
        enfrentamiento.setJuradoRegistro(jurado);
        enfrentamiento.setFechaRegistroResultado(LocalDateTime.now());
        
        enfrentamientoRepository.save(enfrentamiento);
        
        boolean esFinal = esFinalCategoria(enfrentamiento);
        
        if (esFinal) {
            finalizarCategoriaAutomaticamente(enfrentamiento.getCategoria(), ganador);
        } else {
        	avanzarGanadorASiguienteRonda(enfrentamiento, usuarioGanador);
        }
        
        Inscripcion inscripcionGanadora = inscripcionRepository
        	    .findByUsuario_IdUsuarioAndCategoria_IdCategoria(idGanador, enfrentamiento.getCategoria().getIdCategoria())
        	    .orElseThrow(() -> new ResourceNotFoundException("Inscripción del ganador no encontrada"));

        	inscripcionGanadora.setPuntajeAcumulado(inscripcionGanadora.getPuntajeAcumulado() + request.getPuntosOtorgados());
        	inscripcionRepository.save(inscripcionGanadora);
        
        rankingService.recalcularRankingIndividual(enfrentamiento.getCategoria().getIdCategoria());
        rankingService.recalcularRankingClub(enfrentamiento.getCategoria().getIdCategoria());
        
        String nombreCategoria = enfrentamiento.getCategoria().getNombreCategoria();
        
        notificacionService.crearNotificacion(
            idGanador,
            Constants.NOTIF_RESULTADO_REGISTRADO,
            "Victoria registrada",
            "¡Felicidades! Ganaste tu combate en la categoría '" + nombreCategoria + 
            "' y obtuviste " + request.getPuntosOtorgados() + " puntos."
        );
        
        Integer idPerdedor = idGanador.equals(idUsuario1) ? idUsuario2 : idUsuario1;
        
        notificacionService.crearNotificacion(
            idPerdedor,
            Constants.NOTIF_RESULTADO_REGISTRADO,
            "Resultado registrado",
            "Se registró el resultado de tu combate en la categoría '" + nombreCategoria + "'."
        );
    }

    /**
     * Verificar si un enfrentamiento es la FINAL de una categoría
     */
    private boolean esFinalCategoria(Enfrentamiento enfrentamiento) {
        return enfrentamiento.getFase().getIdFase() == 7;
    }
    
    /**
     * Finalizar categoría automáticamente cuando termina la final
     */
    private void finalizarCategoriaAutomaticamente(Categoria categoria, Usuario ganador) {
        
        CatEstado estadoFinalizada = catEstadoRepository.findById(Constants.ESTADO_FINALIZADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Finalizada", "id", Constants.ESTADO_FINALIZADA));
        
        categoria.setEstado(estadoFinalizada);
        categoria.setFechaModificacion(LocalDateTime.now());
        categoriaRepository.save(categoria);
        
        String nombreGanador = ganador.getNombreCompleto();
        String nombreCategoria = categoria.getNombreCategoria();
        
        Robot robotGanador = robotRepository.findByUsuario_IdUsuario(ganador.getIdUsuario())
                .orElse(null);
        
        String nombreRobot = robotGanador != null ? robotGanador.getNombreRobot() : "Sin robot";
        
        List<Usuario> organizadores = usuarioRepository.findByRolId(Constants.ROL_ORGANIZADOR);
        
        for (Usuario organizador : organizadores) {
            notificacionService.crearNotificacion(
                organizador.getIdUsuario(),
                Constants.NOTIF_CATEGORIA_FINALIZADA,
                "Categoría Finalizada",
                "La categoría '" + nombreCategoria + "' ha finalizado. Ganador: " + nombreGanador + " (" + nombreRobot + ")"
            );
        }
        
        List<Inscripcion> inscripciones = inscripcionRepository.findByCategoria_IdCategoria(categoria.getIdCategoria());
        
        for (Inscripcion inscripcion : inscripciones) {
            Integer idCompetidor = inscripcion.getUsuario().getIdUsuario();
            
            if (idCompetidor.equals(ganador.getIdUsuario())) {
                notificacionService.crearNotificacion(
                    idCompetidor,
                    Constants.NOTIF_CATEGORIA_FINALIZADA,
                    "¡Campeón de Categoría!",
                    "¡Felicitaciones! Eres el campeón de la categoría '" + nombreCategoria + "'. ¡Excelente desempeño!"
                );
            } else {
                notificacionService.crearNotificacion(
                    idCompetidor,
                    Constants.NOTIF_CATEGORIA_FINALIZADA,
                    "Categoría Finalizada",
                    "La categoría '" + nombreCategoria + "' ha terminado. Ganador: " + nombreGanador + " (" + nombreRobot + ")"
                );
            }
        }
        
        verificarYNotificarSiTodasCategoriasFinalizadas(categoria.getTorneo());
    }
    
    /**
     * Verificar si todas las categorías de un torneo ya finalizaron
     * Si es así, notificar a los organizadores que pueden finalizar el torneo
     */
    private void verificarYNotificarSiTodasCategoriasFinalizadas(Torneo torneo) {
        
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneo.getIdTorneo());
        
        boolean todasFinalizadas = categorias.stream()
                .allMatch(cat -> cat.getEstado().getIdEstado().equals(Constants.ESTADO_FINALIZADA));
        
        if (todasFinalizadas) {
            List<Usuario> organizadores = usuarioRepository.findByRolId(Constants.ROL_ORGANIZADOR);
            
            for (Usuario organizador : organizadores) {
                notificacionService.crearNotificacion(
                    organizador.getIdUsuario(),
                    Constants.NOTIF_TORNEO_LISTO_FINALIZAR,
                    "Torneo Listo para Finalizar",
                    "Todas las categorías del torneo '" + torneo.getNombreTorneo() + 
                    "' han finalizado. Ya puedes cerrar el torneo oficialmente desde el panel de administración."
                );
            }
        }
    }
    
    /**
     * Obtener enfrentamientos de una categoría
     */
    @Transactional(readOnly = true)
    public List<EnfrentamientoResponse> obtenerEnfrentamientosPorCategoria(Integer categoriaId) {
        List<Enfrentamiento> enfrentamientos = enfrentamientoRepository.findByCategoria_IdCategoria(categoriaId);
        return enfrentamientos.stream()
                .map(EnfrentamientoResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener enfrentamientos pendientes LISTOS (ambos competidores)
     */
    @Transactional(readOnly = true)
    public List<EnfrentamientoResponse> obtenerEnfrentamientosPendientes(Integer categoriaId) {
        List<Enfrentamiento> enfrentamientos = enfrentamientoRepository.findEnfrentamientosListosParaJuez(categoriaId);
        return enfrentamientos.stream()
                .map(EnfrentamientoResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener enfrentamiento por ID
     */
    @Transactional(readOnly = true)
    public Enfrentamiento obtenerEnfrentamientoPorId(Integer enfrentamientoId) {
        return enfrentamientoRepository.findById(enfrentamientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Enfrentamiento", "id", enfrentamientoId));
    }
    
    /**
     * Determinar la fase inicial según el número de competidores
     * Lógica:
     * - 2 competidores → Final (id_fase = 7)
     * - 4 competidores → Semifinal (id_fase = 6)
     * - 8 competidores → Cuartos (id_fase = 5)
     * - 16 competidores → Octavos (id_fase = 4)
     * - 32+ competidores → Ronda 1, 2, 3 (id_fase = 1, 2, 3)
     */
    private CatFase determinarFaseInicial(int numCompetidores) {
        Integer idFase;
        
        switch (numCompetidores) {
            case 2:
                idFase = 7;
                break;
            case 4:
                idFase = 6;
                break;
            case 8:
                idFase = 5;
                break;
            case 16:
                idFase = 4;
                break;
            case 32:
                idFase = 3;
                break;
            case 64:
                idFase = 2;
                break;
            default:
                idFase = 1;
                break;
        }
        
        return catFaseRepository.findById(idFase)
                .orElseThrow(() -> new ResourceNotFoundException("Fase", "id", idFase));
    }
}