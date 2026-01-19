package com.robotech.controller;

import com.robotech.dto.CategoriaRequest;
import com.robotech.dto.CategoriaResponse;
import com.robotech.dto.TorneoRequest;
import com.robotech.dto.TorneoResponse;
import com.robotech.dto.EnfrentamientoResponse;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.CatCategoriaPeso;
import com.robotech.model.CatEstado;
import com.robotech.model.Categoria;
import com.robotech.model.Ranking;
import com.robotech.model.Robot;
import com.robotech.model.Torneo;
import com.robotech.model.Usuario;
import com.robotech.repository.CatCategoriaPesoRepository;
import com.robotech.repository.CatEstadoRepository;
import com.robotech.repository.CategoriaRepository;
import com.robotech.repository.EnfrentamientoRepository;
import com.robotech.repository.RankingRepository;
import com.robotech.repository.RobotRepository;
import com.robotech.repository.TorneoRepository;
import com.robotech.service.CategoriaService;
import com.robotech.service.TorneoService;
import com.robotech.service.UsuarioService;
import com.robotech.service.EnfrentamientoService;
import com.robotech.util.Constants;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizador")
@PreAuthorize("hasRole('ORGANIZADOR')")
public class OrganizadorController {
    
    @Autowired
    private TorneoService torneoService;
    
    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private CatCategoriaPesoRepository catCategoriaPesoRepository;
    
    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;
    
    @Autowired
    private RankingRepository rankingRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private EnfrentamientoService enfrentamientoService;

    /**
     * GET /api/organizador/torneos
     * Obtener todos los torneos ordenados por fecha
     */
    @GetMapping("/torneos")
    public ResponseEntity<List<TorneoResponse>> obtenerTorneos() {
        List<TorneoResponse> torneos = torneoService.obtenerTodosLosTorneos();
        return ResponseEntity.ok(torneos);
    }
    
    /**
     * POST /api/organizador/torneos
     * Crear torneo
     */
    @PostMapping("/torneos")
    public ResponseEntity<TorneoResponse> crearTorneo(@Valid @RequestBody TorneoRequest request) {
        TorneoResponse response = torneoService.crearTorneo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * POST /api/organizador/torneos/{id}/iniciar
     * Iniciar torneo (genera brackets automáticamente)
     */
    @PostMapping("/torneos/{id}/iniciar")
    public ResponseEntity<TorneoResponse> iniciarTorneo(@PathVariable Integer id) {
        TorneoResponse response = torneoService.iniciarTorneo(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/organizador/torneos/{id}/finalizar
     * Finalizar torneo
     */
    @PostMapping("/torneos/{id}/finalizar")
    public ResponseEntity<TorneoResponse> finalizarTorneo(@PathVariable Integer id) {
        TorneoResponse response = torneoService.finalizarTorneo(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/organizador/torneos/{id}/progreso
     * Obtener torneo con progreso detallado de cada categoría
     */
    @GetMapping("/torneos/{id}/progreso")
    public ResponseEntity<Map<String, Object>> obtenerProgresoTorneo(@PathVariable Integer id) {
        TorneoResponse torneo = torneoService.obtenerTorneoPorId(id);
        List<CategoriaResponse> categorias = categoriaService.obtenerCategoriasPorTorneo(id);
        
        List<Map<String, Object>> categoriasConProgreso = new ArrayList<>();
        int totalEnfrentamientosTorneo = 0;
        int totalFinalizadosTorneo = 0;
        
        for (CategoriaResponse categoria : categorias) {
            long totalEnfrentamientos = enfrentamientoRepository
                .countByCategoria_IdCategoria(categoria.getIdCategoria());
            
            long enfrentamientosFinalizados = enfrentamientoRepository
                .countByCategoria_IdCategoriaAndEstadoCombate(
                    categoria.getIdCategoria(), "Finalizado"
                );
            
            long enfrentamientosPendientes = totalEnfrentamientos - enfrentamientosFinalizados;
            
            double progreso = totalEnfrentamientos > 0 ? 
                (enfrentamientosFinalizados * 100.0 / totalEnfrentamientos) : 0;
            
            String ganador = null;
            if ("Finalizada".equals(categoria.getEstado())) {
                List<Ranking> rankings = rankingRepository
                    .findByCategoria_IdCategoriaOrderByPosicionAsc(categoria.getIdCategoria());
                
                if (!rankings.isEmpty()) {
                    Usuario usuario = rankings.get(0).getInscripcion().getUsuario();
                    Robot robot = robotRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElse(null);
                    ganador = robot != null ? robot.getNombreRobot() : usuario.getNombreCompleto();
                }
            }
            
            Map<String, Object> categoriaProgreso = new HashMap<>();
            categoriaProgreso.put("idCategoria", categoria.getIdCategoria());
            categoriaProgreso.put("nombreCategoria", categoria.getNombreCategoria());
            categoriaProgreso.put("totalEnfrentamientos", totalEnfrentamientos);
            categoriaProgreso.put("enfrentamientosPendientes", enfrentamientosPendientes);
            categoriaProgreso.put("enfrentamientosFinalizados", enfrentamientosFinalizados);
            categoriaProgreso.put("progresoPorcentaje", Math.round(progreso * 10) / 10.0);
            categoriaProgreso.put("estado", categoria.getEstado());
            categoriaProgreso.put("ganador", ganador != null ? ganador : "En progreso");
            
            categoriasConProgreso.add(categoriaProgreso);
            totalEnfrentamientosTorneo += totalEnfrentamientos;
            totalFinalizadosTorneo += enfrentamientosFinalizados;
        }
        
        double progresoGeneral = totalEnfrentamientosTorneo > 0 ? 
            (totalFinalizadosTorneo * 100.0 / totalEnfrentamientosTorneo) : 0;
        
        Map<String, Object> response = new HashMap<>();
        response.put("torneo", torneo);
        response.put("categorias", categoriasConProgreso);
        response.put("progresoGeneral", Math.round(progresoGeneral * 10) / 10.0);
        response.put("totalEnfrentamientos", totalEnfrentamientosTorneo);
        response.put("totalFinalizados", totalFinalizadosTorneo);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/organizador/brackets/{categoriaId}
     * Obtener brackets de una categoría específica (para organizador)
     */
    @GetMapping("/brackets/{categoriaId}")
    public ResponseEntity<List<EnfrentamientoResponse>> obtenerBrackets(
            @PathVariable Integer categoriaId) {
        
        List<EnfrentamientoResponse> brackets = enfrentamientoService.obtenerEnfrentamientosPorCategoria(categoriaId);
        return ResponseEntity.ok(brackets);
    }
    
    /**
     * GET /api/organizador/torneos/finalizados
     * Obtener torneos finalizados con sus ganadores
     * (NUEVO - Para Dashboard)
     */
    @GetMapping("/torneos/finalizados")
    public ResponseEntity<List<Map<String, Object>>> obtenerTorneosFinalizados(
        @RequestParam(defaultValue = "5") int limit
    ) {
        List<TorneoResponse> torneos = torneoService.obtenerTorneosFinalizados();
        
        List<Map<String, Object>> torneosConGanadores = torneos.stream()
            .limit(limit)
            .map(torneo -> {
                List<CategoriaResponse> categorias = categoriaService
                    .obtenerCategoriasPorTorneo(torneo.getIdTorneo());
                
                List<Map<String, Object>> categoriasConGanador = categorias.stream()
                    .map(categoria -> {
                        String ganador = "Sin ganador";
                        String nombreClub = "Sin club";
                        
                        List<Ranking> rankings = rankingRepository
                            .findByCategoria_IdCategoriaOrderByPosicionAsc(categoria.getIdCategoria());
                        
                        if (!rankings.isEmpty()) {
                            Usuario usuario = rankings.get(0).getInscripcion().getUsuario();
                            Robot robot = robotRepository
                                .findByUsuario_IdUsuario(usuario.getIdUsuario())
                                .orElse(null);
                            
                            if (robot != null) {
                                ganador = robot.getNombreRobot();
                                if (robot.getClub() != null) {
                                    nombreClub = robot.getClub().getNombreClub();
                                }
                            }
                        }
                        
                        Map<String, Object> categoriaGanador = new HashMap<>();
                        categoriaGanador.put("nombreCategoria", categoria.getNombreCategoria());
                        categoriaGanador.put("ganador", ganador);
                        categoriaGanador.put("club", nombreClub);
                        
                        return categoriaGanador;
                    })
                    .collect(Collectors.toList());
                
                Map<String, Object> torneoConGanadores = new HashMap<>();
                torneoConGanadores.put("torneo", torneo);
                torneoConGanadores.put("categorias", categoriasConGanador);
                
                return torneoConGanadores;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(torneosConGanadores);
    }

    /**
     * GET /api/organizador/torneos/{idTorneo}/categorias
     * Obtener categorías de un torneo con inscritos
     */
    @GetMapping("/torneos/{idTorneo}/categorias")
    public ResponseEntity<List<CategoriaResponse>> obtenerCategorias(@PathVariable Integer idTorneo) {
        List<CategoriaResponse> categorias = categoriaService.obtenerCategoriasPorTorneo(idTorneo);
        return ResponseEntity.ok(categorias);
    }
    
    /**
     * POST /api/organizador/categorias
     * Crear categoría
     */
    @PostMapping("/categorias")
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse categoria = categoriaService.crearCategoria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }
    
    /**
     * PUT /api/organizador/categorias/{id}
     * Editar categoría (solo si NO tiene enfrentamientos)
     */
    @PutMapping("/categorias/{id}")
    public ResponseEntity<Categoria> editarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequest request) {
        
        Categoria categoria = categoriaService.editarCategoria(id, request);
        return ResponseEntity.ok(categoria);
    }
    
    /**
     * DELETE /api/organizador/categorias/{id}
     * Eliminar categoría (solo si NO tiene enfrentamientos)
     */
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/organizador/categorias-peso/disponibles
     * Listar categorías de peso que NO están asignadas al torneo
     */
    @GetMapping("/categorias-peso/disponibles")
    public ResponseEntity<List<CatCategoriaPeso>> listarCategoriasPesoDisponibles(
            @RequestParam Integer torneoId) {
        
        List<CatCategoriaPeso> todasCategorias = catCategoriaPesoRepository.findByActivaTrue();
        
        List<Categoria> categoriasDelTorneo = categoriaRepository.findByTorneo_IdTorneo(torneoId);
        
        List<Integer> idsUsados = categoriasDelTorneo.stream()
            .filter(cat -> cat.getCategoriaPeso() != null)
            .map(cat -> cat.getCategoriaPeso().getIdCategoriaPeso())
            .collect(Collectors.toList());
        
        List<CatCategoriaPeso> disponibles = todasCategorias.stream()
            .filter(cat -> !idsUsados.contains(cat.getIdCategoriaPeso()))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(disponibles);
    }
    
    /**
     * POST /api/organizador/torneos/{torneoId}/categorias/agregar
     * Agregar categoría de peso existente al torneo
     */
    @PostMapping("/torneos/{torneoId}/categorias/agregar")
    public ResponseEntity<Map<String, Object>> agregarCategoriaAlTorneo(
            @PathVariable Integer torneoId,
            @RequestBody Map<String, Object> request) {
        
        try {
            Integer idCategoriaPeso = ((Number) request.get("idCategoriaPeso")).intValue();
            Integer cupoMinimo = ((Number) request.get("cupoMinimo")).intValue();
            Integer cupoMaximo = ((Number) request.get("cupoMaximo")).intValue();
            String descripcion = (String) request.get("descripcion");
            
            if (cupoMinimo % 2 != 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "El cupo mínimo debe ser un número par"));
            }
            
            if (cupoMaximo < cupoMinimo) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "El cupo máximo no puede ser menor al mínimo"));
            }
            
            Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", torneoId));
            
            if (!torneo.getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "No se pueden agregar categorías a un torneo iniciado"));
            }
            
            CatCategoriaPeso categoriaPeso = catCategoriaPesoRepository.findById(idCategoriaPeso)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", idCategoriaPeso));
            
            List<Categoria> categoriasExistentes = categoriaRepository.findByTorneo_IdTorneo(torneoId);
            boolean yaExiste = categoriasExistentes.stream()
                .anyMatch(cat -> cat.getCategoriaPeso() != null && 
                                 cat.getCategoriaPeso().getIdCategoriaPeso().equals(idCategoriaPeso));
            
            if (yaExiste) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Esta categoría ya está asignada al torneo"));
            }
            
            CatEstado estadoActiva = catEstadoRepository.findById(Constants.ESTADO_ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Activa no encontrado"));
            
            Categoria nuevaCategoria = new Categoria();
            nuevaCategoria.setNombreCategoria(categoriaPeso.getNombre());
            nuevaCategoria.setDescripcion(descripcion != null ? descripcion : categoriaPeso.getDescripcion());
            nuevaCategoria.setCupoMinimo(cupoMinimo);
            nuevaCategoria.setCupoMaximo(cupoMaximo);
            nuevaCategoria.setTorneo(torneo);
            nuevaCategoria.setEstado(estadoActiva);
            nuevaCategoria.setCategoriaPeso(categoriaPeso);
            nuevaCategoria.setTieneEnfrentamientos(false);
            
            categoriaRepository.save(nuevaCategoria);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Categoría agregada exitosamente",
                "data", Map.of(
                    "idCategoria", nuevaCategoria.getIdCategoria(),
                    "nombreCategoria", nuevaCategoria.getNombreCategoria()
                )
            ));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error al agregar categoría: " + e.getMessage()));
        }
    }

    /**
     * GET /api/organizador/estadisticas
     * KPIs para el dashboard del organizador
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        long totalTorneos = torneoService.obtenerTodosLosTorneos().size();
        
        long torneosActivos = torneoService.obtenerTorneosPorEstado(Constants.ESTADO_EN_CURSO).size();
        
        long totalCategorias = catCategoriaPesoRepository.count();
        
        long categoriasActivas = categoriaRepository.countCategoriasActivas(
            Constants.ESTADO_EN_CURSO, 
            Constants.ESTADO_CERRADA
        );
        
        long totalEnfrentamientos = enfrentamientoRepository.count();
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalTorneos", totalTorneos);
        estadisticas.put("torneosActivos", torneosActivos);
        estadisticas.put("totalCategorias", totalCategorias);
        estadisticas.put("categoriasActivas", categoriasActivas);
        estadisticas.put("totalEnfrentamientos", totalEnfrentamientos);
        
        return ResponseEntity.ok(estadisticas);
    }
    
    /**
     * GET /api/organizador/categorias-peso
     * Listar TODAS las categorías de peso (activas e inactivas)
     */
    @GetMapping("/categorias-peso")
    public ResponseEntity<List<CatCategoriaPeso>> listarTodasCategoriasPeso() {
        List<CatCategoriaPeso> categorias = catCategoriaPesoRepository.findAll();
        return ResponseEntity.ok(categorias);
    }
    
    /**
     * POST /api/organizador/categorias-peso
     * Crear nueva categoría de peso
     */
    @PostMapping("/categorias-peso")
    public ResponseEntity<Map<String, Object>> crearCategoriaPeso(
            @RequestBody Map<String, Object> request) {
        
        try {
            String nombre = (String) request.get("nombreCategoriaPeso");
            String descripcion = (String) request.get("descripcion");
            Double pesoMinimo = ((Number) request.get("pesoMinimo")).doubleValue();
            Double pesoMaximo = ((Number) request.get("pesoMaximo")).doubleValue();
            Double dimensionMaxima = request.get("dimensionMaxima") != null ? 
                ((Number) request.get("dimensionMaxima")).doubleValue() : null;
            
            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El nombre de la categoría es obligatorio"));
            }
            
            if (pesoMinimo >= pesoMaximo) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El peso máximo debe ser mayor al peso mínimo"));
            }
            
            Map<String, Object> resultado = usuarioService.crearCategoriaPeso(
                nombre, descripcion, pesoMinimo, pesoMaximo, dimensionMaxima
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al crear categoría: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /api/organizador/categorias-peso/{id}
     * Editar categoría de peso
     */
    @PutMapping("/categorias-peso/{id}")
    public ResponseEntity<Map<String, Object>> editarCategoriaPeso(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        
        try {
            String nombre = (String) request.get("nombreCategoriaPeso");
            String descripcion = (String) request.get("descripcion");
            Double pesoMinimo = ((Number) request.get("pesoMinimo")).doubleValue();
            Double pesoMaximo = ((Number) request.get("pesoMaximo")).doubleValue();
            Double dimensionMaxima = request.get("dimensionMaxima") != null ? 
                ((Number) request.get("dimensionMaxima")).doubleValue() : null;
            
            if (pesoMinimo >= pesoMaximo) {
                return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El peso máximo debe ser mayor al peso mínimo"));
            }
            
            Map<String, Object> resultado = usuarioService.editarCategoriaPeso(
                id, nombre, descripcion, pesoMinimo, pesoMaximo, dimensionMaxima
            );
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al editar categoría: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /api/organizador/categorias-peso/{id}/activar
     * Activar categoría de peso
     */
    @PutMapping("/categorias-peso/{id}/activar")
    public ResponseEntity<Map<String, Object>> activarCategoriaPeso(@PathVariable Integer id) {
        try {
            Map<String, Object> resultado = usuarioService.cambiarEstadoCategoriaPeso(id, true);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al activar categoría: " + e.getMessage()));
        }
    }
    
    /**
     * PUT /api/organizador/categorias-peso/{id}/desactivar
     * Desactivar categoría de peso
     */
    @PutMapping("/categorias-peso/{id}/desactivar")
    public ResponseEntity<Map<String, Object>> desactivarCategoriaPeso(@PathVariable Integer id) {
        try {
            Map<String, Object> resultado = usuarioService.cambiarEstadoCategoriaPeso(id, false);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al desactivar categoría: " + e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/organizador/categorias-peso/{id}
     * Eliminar categoría de peso (solo si NO está en uso)
     */
    @DeleteMapping("/categorias-peso/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCategoriaPeso(@PathVariable Integer id) {
        try {
            Map<String, Object> resultado = usuarioService.eliminarCategoriaPeso(id);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al eliminar categoría: " + e.getMessage()));
        }
    }
}