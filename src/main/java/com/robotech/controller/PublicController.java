package com.robotech.controller;

import com.robotech.dto.ClubSimpleResponse;
import com.robotech.dto.RankingClubResponse;
import com.robotech.dto.RankingResponse;
import com.robotech.dto.TorneoResponse;
import com.robotech.dto.TorneoResultadoResponse;
import com.robotech.model.CatCategoriaPeso;
import com.robotech.repository.CatCategoriaPesoRepository;
import com.robotech.service.ClubService;
import com.robotech.service.RankingService;
import com.robotech.service.TorneoService;
import com.robotech.dto.CategoriaPesoResponse;
import com.robotech.dto.CategoriaResponse;
import com.robotech.service.CategoriaService;
import com.robotech.dto.SedeResponse;
import com.robotech.service.SedeService;
import com.robotech.dto.ClubHistorialResponse;
import com.robotech.dto.EnfrentamientoResponse;
import com.robotech.model.Categoria;
import com.robotech.repository.CategoriaRepository;
import com.robotech.service.EnfrentamientoService;
import com.robotech.exception.BusinessException;
import com.robotech.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    
    @Autowired
    private RankingService rankingService;
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private TorneoService torneoService;
    
    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private CatCategoriaPesoRepository catCategoriaPesoRepository;
    
    @Autowired
    private SedeService sedeService;
    
    @Autowired
    private EnfrentamientoService enfrentamientoService;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    /**
     * GET /api/public/rankings/competidores?categoria_id={id}
     * Obtener ranking individual público
     */
    @GetMapping("/rankings/competidores")
    public ResponseEntity<List<RankingResponse>> obtenerRankingCompetidores(
            @RequestParam("categoria_id") Integer categoriaId) {
        List<RankingResponse> ranking = rankingService.obtenerRankingIndividual(categoriaId, null);
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * GET /api/public/rankings/clubes?categoria_id={id}
     * Obtener ranking de clubes público
     */
    @GetMapping("/rankings/clubes")
    public ResponseEntity<List<RankingClubResponse>> obtenerRankingClubes(
            @RequestParam("categoria_id") Integer categoriaId) {
        List<RankingClubResponse> ranking = rankingService.obtenerRankingClubes(categoriaId, null);
        return ResponseEntity.ok(ranking);
    }
    
    /**
     * GET /api/public/clubes?search={texto}&ordenar={campo}
     * Obtener directorio público de clubes
     */
    @GetMapping("/clubes")
    public ResponseEntity<List<ClubSimpleResponse>> obtenerClubesPublicos(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ordenar) {
        List<ClubSimpleResponse> clubes = clubService.obtenerClubesPublicos(search, ordenar);
        return ResponseEntity.ok(clubes);
    }
    
    /**
     * GET /api/public/torneos
     * Obtener todos los torneos
     */
    @GetMapping("/torneos")
    public ResponseEntity<List<TorneoResponse>> obtenerTorneos() {
        List<TorneoResponse> torneos = torneoService.obtenerTodosLosTorneos();
        return ResponseEntity.ok(torneos);
    }
    
    /**
     * GET /api/public/torneos/{id}/categorias
     * Obtener categorías de un torneo
     */
    @GetMapping("/torneos/{id}/categorias")
    public ResponseEntity<List<CategoriaResponse>> obtenerCategoriasTorneo(@PathVariable Integer id) {
        List<CategoriaResponse> categorias = categoriaService.obtenerCategoriasPublicasPorTorneo(id);
        return ResponseEntity.ok(categorias);
    }

    
    /**
     * GET /api/public/torneos/finalizados
     * Obtener torneos finalizados
     */
    @GetMapping("/torneos/finalizados")
    public ResponseEntity<List<TorneoResponse>> obtenerTorneosFinalizados() {
        List<TorneoResponse> torneos = torneoService.obtenerTorneosFinalizados();
        return ResponseEntity.ok(torneos);
    }
    
    /**
     * GET /api/public/torneos/{id}
     * Obtener torneo por ID
     */
    @GetMapping("/torneos/{id}")
    public ResponseEntity<TorneoResponse> obtenerTorneoPorId(@PathVariable Integer id) {
        TorneoResponse torneo = torneoService.obtenerTorneoPorId(id);
        return ResponseEntity.ok(torneo);
    }
    
    /**
     * GET /api/public/torneos/{id}/resultados
     * Obtener resultados completos de un torneo (podio, ranking completo, estadísticas)
     */
    @GetMapping("/torneos/{id}/resultados")
    public ResponseEntity<TorneoResultadoResponse> obtenerResultadosTorneo(@PathVariable Integer id) {
        TorneoResultadoResponse resultados = torneoService.obtenerResultadosTorneo(id);
        return ResponseEntity.ok(resultados);
    }
    
    /**
     * GET /api/public/categorias-peso
     * Listar categorías de peso disponibles
     */
    @GetMapping("/categorias-peso")
    public ResponseEntity<List<CategoriaPesoResponse>> listarCategoriasPeso() {
        List<CategoriaPesoResponse> categorias = categoriaService.listarCategoriasActivas();
        return ResponseEntity.ok(categorias);
    }
    
    /**
     * GET /api/public/sedes
     * Listar todas las sedes disponibles
     */
    @GetMapping("/sedes")
    public ResponseEntity<List<SedeResponse>> listarSedes() {
        List<SedeResponse> sedes = sedeService.listarSedes();
        return ResponseEntity.ok(sedes);
    }
    
    /**
     * GET /api/public/sedes/{id}
     * Obtener detalle de una sede
     */
    @GetMapping("/sedes/{id}")
    public ResponseEntity<SedeResponse> obtenerSede(@PathVariable Integer id) {
        SedeResponse sede = sedeService.obtenerSedePorId(id);
        return ResponseEntity.ok(sede);
    }
    
    /**
     * GET /api/public/clubes/{id}/historial?categoriaId={id}
     * Obtener historial detallado de un club
     */
    @GetMapping("/clubes/{id}/historial")
    public ResponseEntity<ClubHistorialResponse> obtenerHistorialClub(
            @PathVariable Integer id,
            @RequestParam("categoriaId") Integer categoriaId) {
        
        ClubHistorialResponse historial = clubService.obtenerHistorialClub(id, categoriaId);
        return ResponseEntity.ok(historial);
    }
    
    /**
     * GET /api/public/brackets/{categoriaId}
     * Obtener brackets de una categoría (público)
     */
    @Transactional(readOnly = true)
    @GetMapping("/brackets/{categoriaId}")
    public ResponseEntity<List<EnfrentamientoResponse>> obtenerBracketsPublicos(
            @PathVariable Integer categoriaId) {
        
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new BusinessException("Categoría no encontrada"));
        
        Integer estadoIdTorneo = categoria.getTorneo().getEstado().getIdEstado();
        if (!Constants.ESTADO_EN_CURSO.equals(estadoIdTorneo) && 
            !Constants.ESTADO_FINALIZADO.equals(estadoIdTorneo)) {
            throw new BusinessException("Los brackets aún no están disponibles públicamente");
        }
        
        List<EnfrentamientoResponse> brackets = enfrentamientoService.obtenerEnfrentamientosPorCategoria(categoriaId);
        return ResponseEntity.ok(brackets);
    }
    
    /**
     * GET /api/public/categorias-peso/activas
     * Obtener solo categorías ACTIVAS
     */
    @GetMapping("/categorias-peso/activas")
    public ResponseEntity<Map<String, Object>> obtenerCategoriasActivas() {
        try {
            List<CatCategoriaPeso> categorias = catCategoriaPesoRepository.findByActivaTrue();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categorias);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener categorías: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}