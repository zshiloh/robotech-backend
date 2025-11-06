package com.robotech.controller;

import com.robotech.model.Competidor;
import com.robotech.service.CompetidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/competidores")
@CrossOrigin(origins = "*")
public class CompetidorController {

    @Autowired
    private CompetidorService competidorService;

    /**
     * CU05: Inscribir nuevo competidor
     * POST /api/competidores
     */
    @PostMapping
    public ResponseEntity<?> inscribir(@RequestBody Competidor competidor) {
        try {
            Competidor nuevoCompetidor = competidorService.inscribirCompetidor(competidor);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Competidor inscrito exitosamente");
            response.put("competidor", nuevoCompetidor);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al inscribir competidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar todos los competidores
     * GET /api/competidores
     */
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<Competidor> competidores = competidorService.listarTodos();
            return ResponseEntity.ok(competidores);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar competidores: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar competidores activos
     * GET /api/competidores/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos() {
        try {
            List<Competidor> competidores = competidorService.listarActivos();
            return ResponseEntity.ok(competidores);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar competidores activos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar competidores por club
     * GET /api/competidores/club/{idClub}
     */
    @GetMapping("/club/{idClub}")
    public ResponseEntity<?> listarPorClub(@PathVariable Integer idClub) {
        try {
            List<Competidor> competidores = competidorService.listarPorClub(idClub);
            return ResponseEntity.ok(competidores);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar competidores del club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar competidores por categoría
     * GET /api/competidores/categoria/{idCategoria}
     */
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<?> listarPorCategoria(@PathVariable Integer idCategoria) {
        try {
            List<Competidor> competidores = competidorService.listarPorCategoria(idCategoria);
            return ResponseEntity.ok(competidores);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar competidores de la categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Buscar competidor por ID
     * GET /api/competidores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return competidorService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al buscar competidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Actualizar competidor
     * PUT /api/competidores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Competidor competidor) {
        try {
            Competidor competidorActualizado = competidorService.actualizarCompetidor(id, competidor);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Competidor actualizado exitosamente");
            response.put("competidor", competidorActualizado);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar competidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Activar/Desactivar competidor
     * PATCH /api/competidores/{id}/estado
     * Body: { "activo": true }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        try {
            Boolean activo = body.get("activo");
            Competidor competidor = competidorService.cambiarEstado(id, activo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estado actualizado exitosamente");
            response.put("competidor", competidor);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al cambiar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Eliminar competidor
     * DELETE /api/competidores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            competidorService.eliminarCompetidor(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Competidor eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar competidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}