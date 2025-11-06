package com.robotech.controller;

import com.robotech.model.Usuario;
import com.robotech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Listar todos los usuarios
     * GET /api/usuarios
     */
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        try {
            List<Usuario> usuarios = usuarioService.listarTodos();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar usuarios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Listar usuarios activos
     * GET /api/usuarios/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos() {
        try {
            List<Usuario> usuarios = usuarioService.listarActivos();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al listar usuarios activos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Buscar usuario por ID
     * GET /api/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return usuarioService.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al buscar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * CU15: Actualizar rol de un usuario
     * PUT /api/usuarios/{id}/rol
     * Body: { "idRol": 2 }
     */
    @PutMapping("/{id}/rol")
    public ResponseEntity<?> actualizarRol(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        try {
            Integer idRol = body.get("idRol");
            Usuario usuario = usuarioService.actualizarRol(id, idRol);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rol actualizado exitosamente");
            response.put("usuario", usuario);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al actualizar rol: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Crear nuevo usuario
     * POST /api/usuarios
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario creado exitosamente");
            response.put("usuario", nuevoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            // Si el error es por duplicado, usar código 409 (CONFLICT)
            if (e.getMessage().contains("ya está registrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al crear usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Activar/Desactivar usuario
     * PATCH /api/usuarios/{id}/estado
     * Body: { "activo": true }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        try {
            Boolean activo = body.get("activo");
            Usuario usuario = usuarioService.cambiarEstado(id, activo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estado actualizado exitosamente");
            response.put("usuario", usuario);
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