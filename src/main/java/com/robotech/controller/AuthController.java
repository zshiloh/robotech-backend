package com.robotech.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestParam;

import com.robotech.model.Usuario;
import com.robotech.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * CU14: Login
     * POST /api/auth/login
     * Body: { "correo": "admin@robotech.com", "password": "admin123" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        try {
            String correo = credenciales.get("correo");
            String password = credenciales.get("password");

            Optional<Usuario> usuario = usuarioService.autenticar(correo, password);

            if (usuario.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("usuario", usuario.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Credenciales inválidas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error al iniciar sesión: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verificar si el servidor está activo
     * GET /api/auth/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "ROBOTECH API está funcionando");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    /**
     * TEMPORAL: Generar hash de password
     * GET /api/auth/hash?password=admin123
     */
    @GetMapping("/hash")
    public ResponseEntity<?> hashPassword(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        return ResponseEntity.ok(response);
    }
}