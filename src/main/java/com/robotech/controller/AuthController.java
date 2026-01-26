package com.robotech.controller;

import com.robotech.dto.LoginRequest;
import com.robotech.dto.LoginResponse;
import com.robotech.dto.RegistroConClubRequest;
import com.robotech.dto.RegistroRequest;
import com.robotech.dto.ResetearPasswordRequest;
import com.robotech.dto.SolicitarRecuperacionRequest;
import com.robotech.dto.UsuarioResponse;
import com.robotech.dto.VerificarRespuestaRequest;
import com.robotech.service.AuthService;
import com.robotech.service.RecuperacionPasswordService;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * POST /api/auth/login
     * Login de usuario
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/registro-con-club
     * Registro de usuario con club
     */
    @PostMapping("/registro-con-club")
    public ResponseEntity<UsuarioResponse> registroConClub(@Valid @RequestBody RegistroConClubRequest request) {
        UsuarioResponse response = authService.registroConClub(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Autowired
    private RecuperacionPasswordService recuperacionService;

    /**
     * POST /api/auth/registro
     * Registro de usuario simple
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse response = authService.registro(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/auth/verificar-email-recuperacion
     * Verificar email y obtener pregunta de seguridad
     */
    @PostMapping("/verificar-email-recuperacion")
    public ResponseEntity<Map<String, Object>> verificarEmailRecuperacion(
            @Valid @RequestBody SolicitarRecuperacionRequest request) {
        
        Map<String, Object> response = recuperacionService.verificarEmail(request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/verificar-respuesta-seguridad
     * Verificar respuesta y generar token
     */
    @PostMapping("/verificar-respuesta-seguridad")
    public ResponseEntity<Map<String, Object>> verificarRespuestaSeguridad(
            @Valid @RequestBody VerificarRespuestaRequest request) {
        
        Map<String, Object> response = recuperacionService.verificarRespuestaYGenerarToken(
            request.getEmail(), 
            request.getRespuesta()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/resetear-password
     * Resetear contraseña con token
     */
    @PostMapping("/resetear-password")
    public ResponseEntity<Map<String, String>> resetearPassword(
            @Valid @RequestBody ResetearPasswordRequest request) {
        
        recuperacionService.resetearPassword(
            request.getEmail(), 
            request.getToken(), 
            request.getPasswordNueva()
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Contraseña cambiada exitosamente");
        
        return ResponseEntity.ok(response);
    }
}