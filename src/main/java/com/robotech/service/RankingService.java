package com.robotech.service;

import com.robotech.dto.RankingClubResponse;
import com.robotech.dto.RankingResponse;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.*;
import com.robotech.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {
    
    @Autowired
    private RankingRepository rankingRepository;
    
    @Autowired
    private RankingClubRepository rankingClubRepository;
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    /**
     * Recalcular ranking individual de una categoría
     */
    @Transactional
    public void recalcularRankingIndividual(Integer idCategoria) {
    	
        List<Inscripcion> inscripciones = inscripcionRepository
                .findByCategoriOrderByPuntaje(idCategoria);
        
        rankingRepository.deleteByCategoria(idCategoria);
        
        int posicion = 1;
        
        for (Inscripcion inscripcion : inscripciones) {
        	
            long victorias = enfrentamientoRepository
                    .countVictoriasByUsuarioAndCategoria(
                        inscripcion.getUsuario().getIdUsuario(), 
                        idCategoria
                    );
            
            List<Enfrentamiento> enfrentamientos = enfrentamientoRepository
                    .findEnfrentamientosFinalizadosByUsuario(
                        inscripcion.getUsuario().getIdUsuario()
                    );
            
            long enfrentamientosCat = enfrentamientos.stream()
                    .filter(e -> e.getCategoria().getIdCategoria().equals(idCategoria))
                    .count();
            
            int derrotas = (int) (enfrentamientosCat - victorias);
            
            Ranking ranking = new Ranking();
            ranking.setCategoria(inscripcion.getCategoria());
            ranking.setInscripcion(inscripcion);
            ranking.setPosicion(posicion);
            ranking.setPuntosTotales(inscripcion.getPuntajeAcumulado());
            ranking.setVictorias((int) victorias);
            ranking.setDerrotas(derrotas);
            ranking.setFechaActualizacion(LocalDateTime.now());
            
            rankingRepository.save(ranking);
            
            posicion++;
        }
    }
    
    /**
     * Recalcular ranking de clubes de una categoría
     */
    @Transactional
    public void recalcularRankingClub(Integer idCategoria) {
    	
        List<Inscripcion> inscripciones = inscripcionRepository
                .findByCategoria_IdCategoria(idCategoria);
        
        var clubMap = new java.util.HashMap<Integer, ClubStats>();
        
        for (Inscripcion inscripcion : inscripciones) {
        	
            robotRepository.findByUsuario_IdUsuario(inscripcion.getUsuario().getIdUsuario())
                    .ifPresent(robot -> {
                        if (robot.getClub() != null && robot.getEstado() == Robot.EstadoRobot.ACTIVO) {
                            Integer clubId = robot.getClub().getIdClub();
                            
                            ClubStats stats = clubMap.getOrDefault(clubId, new ClubStats(clubId));
                            
                            stats.puntosTotales += inscripcion.getPuntajeAcumulado();
                            
                            long victorias = enfrentamientoRepository
                                    .countVictoriasByUsuarioAndCategoria(
                                        inscripcion.getUsuario().getIdUsuario(), 
                                        idCategoria
                                    );
                            stats.victoriaTotales += victorias;
                            
                            List<Enfrentamiento> enfrentamientos = enfrentamientoRepository
                                    .findEnfrentamientosFinalizadosByUsuario(
                                        inscripcion.getUsuario().getIdUsuario()
                                    );
                            
                            long enfrentamientosCat = enfrentamientos.stream()
                                    .filter(e -> e.getCategoria().getIdCategoria().equals(idCategoria))
                                    .count();
                            
                            long derrotas = enfrentamientosCat - victorias;
                            stats.derrotasTotales += derrotas;
                            
                            stats.competidoresActivos++;
                            
                            clubMap.put(clubId, stats);
                        }
                    });
        }
        
        rankingClubRepository.deleteByCategoria(idCategoria);
        
        List<ClubStats> clubesList = new ArrayList<>(clubMap.values());
        clubesList.sort((c1, c2) -> Integer.compare(c2.puntosTotales, c1.puntosTotales));
        
        int posicion = 1;
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", idCategoria));
        
        for (ClubStats stats : clubesList) {
            RankingClub rankingClub = new RankingClub();
            rankingClub.setCategoria(categoria);
            rankingClub.setClub(stats.getClub());
            rankingClub.setPosicionClub(posicion);
            rankingClub.setPuntosTotalesClub(stats.puntosTotales);
            rankingClub.setVictoriaTotales((int) stats.victoriaTotales);
            rankingClub.setDerrotasTotales((int) stats.derrotasTotales);
            rankingClub.setCompetidoresActivos(stats.competidoresActivos);
            rankingClub.setFechaActualizacion(LocalDateTime.now());
            
            rankingClubRepository.save(rankingClub);
            
            posicion++;
        }
    }
    
    /**
     * Recalcular rankings de club para todas las categorías
     */
    @Transactional
    public void recalcularRankingClubParaTodasLasCategorias(Integer idClub) {
        List<Inscripcion> inscripciones = inscripcionRepository.findAll();
        
        var categoriasAfectadas = new java.util.HashSet<Integer>();
        
        for (Inscripcion inscripcion : inscripciones) {
            robotRepository.findByUsuario_IdUsuario(inscripcion.getUsuario().getIdUsuario())
                    .ifPresent(robot -> {
                        if (robot.getClub() != null && robot.getClub().getIdClub().equals(idClub)) {
                            categoriasAfectadas.add(inscripcion.getCategoria().getIdCategoria());
                        }
                    });
        }
        
        for (Integer idCategoria : categoriasAfectadas) {
            recalcularRankingClub(idCategoria);
        }
    }
    
    /**
     * Obtener ranking individual público de una categoría
     */
    @Transactional(readOnly = true)
    public List<RankingResponse> obtenerRankingIndividual(Integer idCategoria, Integer idUsuarioActual) {
        List<Ranking> rankings = rankingRepository.findByCategoria(idCategoria);
        
        List<RankingResponse> responses = new ArrayList<>();
        
        for (Ranking ranking : rankings) {
            Inscripcion inscripcion = ranking.getInscripcion();
            Usuario usuario = inscripcion.getUsuario();
            
            String nombreClub = null;
            Robot robot = robotRepository.findByUsuario_IdUsuario(usuario.getIdUsuario()).orElse(null);
            
            if (robot != null && robot.getClub() != null) {
                nombreClub = robot.getClub().getNombreClub();
            }
            
            String nombreMostrar = usuario.getNombreCompleto();
            String apodo = null;

            if (usuario.getApodoPersonal() != null && !usuario.getApodoPersonal().trim().isEmpty()) {
                nombreMostrar = usuario.getApodoPersonal();
                apodo = usuario.getApodoPersonal();
            }
            
            boolean esUsuarioActual = idUsuarioActual != null && 
                                     usuario.getIdUsuario().equals(idUsuarioActual);
            
            RankingResponse response = new RankingResponse(
                ranking.getPosicion(),
                nombreMostrar,
                apodo,
                nombreClub,
                ranking.getPuntosTotales(),
                ranking.getVictorias(),
                ranking.getDerrotas(),
                esUsuarioActual
            );
            
            responses.add(response);
        }
        
        return responses;
    }
    
    /**
     * Obtener ranking de clubes público de una categoría
     */
    @Transactional(readOnly = true)
    public List<RankingClubResponse> obtenerRankingClubes(Integer idCategoria, Integer idUsuarioActual) {
        List<RankingClub> rankings = rankingClubRepository.findByCategoria(idCategoria);
        
        List<RankingClubResponse> responses = new ArrayList<>();
        
        Integer clubIdUsuarioActual = null;
        if (idUsuarioActual != null) {
            Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuarioActual).orElse(null);
            if (robot != null && robot.getClub() != null) {
                clubIdUsuarioActual = robot.getClub().getIdClub();
            }
        }
        
        for (RankingClub ranking : rankings) {
            Club club = ranking.getClub();
            
            boolean esClubDelUsuario = clubIdUsuarioActual != null && 
                                      club.getIdClub().equals(clubIdUsuarioActual);
            
            RankingClubResponse response = new RankingClubResponse(
                ranking.getPosicionClub(),
                club.getNombreClub(),
                club.getLogoUrl(),
                ranking.getPuntosTotalesClub(),
                ranking.getVictoriaTotales(),
                ranking.getDerrotasTotales(),
                ranking.getCompetidoresActivos(),
                esClubDelUsuario
            );
            
            responses.add(response);
        }
        
        return responses;
    }
    
    /**
     * Clase auxiliar para agrupar estadísticas de clubes
     */
    private class ClubStats {
        Integer clubId;
        int puntosTotales = 0;
        long victoriaTotales = 0;
        long derrotasTotales = 0;
        int competidoresActivos = 0;
        Club club;
        
        public ClubStats(Integer clubId) {
            this.clubId = clubId;
            this.club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        }
        
        public Club getClub() {
            return this.club;
        }
    }
}