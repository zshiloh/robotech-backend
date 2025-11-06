package com.robotech.controller;

import com.robotech.model.Categoria;
import com.robotech.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    /**
     * CU07: Crear nueva categoría
     * POST /api/categorias
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Categoria categoria) {
        try {
            Categoria nuevaCategoria = categoriaService.crearCategoria(categoria);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría creada exitosamente");
            response.put("categoria", nuevaCategoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar todas las categorías
     * GET /api/categorias
     */
    @GetMapping
    public ResponseEntity<?> listarTodas() {
        try {
            List<Categoria> categorias = categoriaService.listarTodas();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar categorías: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar categorías activas
     * GET /api/categorias/activas
     */
    @GetMapping("/activas")
    public ResponseEntity<?> listarActivas() {
        try {
            List<Categoria> categorias = categoriaService.listarActivas();
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar categorías activas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Buscar categoría por ID
     * GET /api/categorias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return categoriaService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al buscar categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CU07: Actualizar categoría
     * PUT /api/categorias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Categoria categoria) {
        try {
            Categoria categoriaActualizada = categoriaService.actualizarCategoria(id, categoria);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría actualizada exitosamente");
            response.put("categoria", categoriaActualizada);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CU07: Eliminar categoría
     * DELETE /api/categorias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            categoriaService.eliminarCategoria(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Activar/Desactivar categoría
     * PATCH /api/categorias/{id}/estado
     * Body: { "activo": true }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        try {
            Boolean activo = body.get("activo");
            Categoria categoria = categoriaService.cambiarEstado(id, activo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estado actualizado exitosamente");
            response.put("categoria", categoria);
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
}