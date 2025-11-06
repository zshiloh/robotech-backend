package com.robotech.controller;

import com.robotech.model.Club;
import com.robotech.service.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clubes")
@CrossOrigin(origins = "*")
public class ClubController {

    @Autowired
    private ClubService clubService;

    /**
     * CU01: Registrar nuevo club
     * POST /api/clubes
     */
    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Club club) {
        try {
            Club nuevoClub = clubService.registrarClub(club);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Club registrado exitosamente con estado Pendiente");
            response.put("club", nuevoClub);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al registrar club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar todos los clubes
     * GET /api/clubes
     */
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<Club> clubes = clubService.listarTodos();
            return ResponseEntity.ok(clubes);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar clubes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CU02: Listar clubes pendientes de validación
     * GET /api/clubes/pendientes
     */
    @GetMapping("/pendientes")
    public ResponseEntity<?> listarPendientes() {
        try {
            List<Club> clubes = clubService.listarPendientes();
            return ResponseEntity.ok(clubes);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar clubes pendientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar clubes activos
     * GET /api/clubes/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos() {
        try {
            List<Club> clubes = clubService.listarActivos();
            return ResponseEntity.ok(clubes);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar clubes activos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Buscar club por ID
     * GET /api/clubes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return clubService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al buscar club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CU02: Aprobar club
     * PUT /api/clubes/{id}/aprobar
     * Body: { "idAdminValidador": 1 }
     */
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        try {
            Integer idAdmin = body.get("idAdminValidador");
            Club club = clubService.aprobarClub(id, idAdmin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Club aprobado exitosamente");
            response.put("club", club);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al aprobar club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CU02: Rechazar club
     * PUT /api/clubes/{id}/rechazar
     * Body: { "idAdminValidador": 1, "observaciones": "Datos incorrectos" }
     */
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazar(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        try {
            Integer idAdmin = (Integer) body.get("idAdminValidador");
            String observaciones = (String) body.get("observaciones");
            Club club = clubService.rechazarClub(id, idAdmin, observaciones);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Club rechazado");
            response.put("club", club);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al rechazar club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Actualizar club
     * PUT /api/clubes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Club club) {
        try {
            Club clubActualizado = clubService.actualizarClub(id, club);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Club actualizado exitosamente");
            response.put("club", clubActualizado);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Eliminar club
     * DELETE /api/clubes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            clubService.eliminarClub(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Club eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al eliminar club: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}