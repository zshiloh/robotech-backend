package com.robotech.service;

import com.robotech.exception.BusinessException;
import com.robotech.model.Usuario;
import com.robotech.repository.UsuarioRepository;
import com.robotech.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class RecuperacionPasswordService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final int MAX_INTENTOS = 3;
    private static final int MINUTOS_BLOQUEO = 15;
    
    /**
     * Verificar email y retornar pregunta de seguridad
     */
    public Map<String, Object> verificarEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("El email no está registrado"));
        
        if (usuario.getPreguntaSeguridad() == null || usuario.getRespuestaSeguridadHash() == null) {
            throw new BusinessException(
                "Este usuario no tiene pregunta de seguridad configurada. " +
                "Contacta al administrador para recuperar tu cuenta."
            );
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("emailExiste", true);
        response.put("preguntaSeguridad", usuario.getPreguntaSeguridad());
        
        return response;
    }
    
    /**
     * Verificar respuesta de seguridad y generar token
     */
    @Transactional
    public Map<String, Object> verificarRespuestaYGenerarToken(String email, String respuesta) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("El email no está registrado"));
        
        verificarBloqueoPorIntentos(usuario);
        
        String respuestaNormalizada = respuesta.toLowerCase().trim();
        
        if (!passwordEncoder.matches(respuestaNormalizada, usuario.getRespuestaSeguridadHash())) {
            usuario.setIntentosRecuperacion(usuario.getIntentosRecuperacion() + 1);
            usuario.setFechaUltimoIntentoRecuperacion(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            int intentosRestantes = MAX_INTENTOS - usuario.getIntentosRecuperacion();
            
            if (intentosRestantes > 0) {
                throw new BusinessException(
                    "Respuesta incorrecta. Intentos restantes: " + intentosRestantes
                );
            } else {
                throw new BusinessException(
                    "Has alcanzado el límite de intentos. " +
                    "Inténtalo nuevamente en " + MINUTOS_BLOQUEO + " minutos."
                );
            }
        }
        
        String token = generarTokenAleatorio();
        
        usuario.setTokenRecuperacion(token);
        usuario.setTokenExpiracion(LocalDateTime.now().plusMinutes(15));
        usuario.setIntentosRecuperacion(0);
        usuario.setFechaUltimoIntentoRecuperacion(null);
        usuarioRepository.save(usuario);
        
        Map<String, Object> response = new HashMap<>();
        response.put("correcto", true);
        response.put("token", token);
        response.put("expiraEn", 15);
        
        return response;
    }
    
    /**
     * Paso 3: Resetear contraseña con token
     */
    @Transactional
    public void resetearPassword(String email, String token, String passwordNueva) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("El email no está registrado"));
        
        if (usuario.getTokenRecuperacion() == null || 
            !usuario.getTokenRecuperacion().equals(token)) {
            throw new BusinessException("Código de recuperación inválido");
        }
        
        if (usuario.getTokenExpiracion() == null || 
            LocalDateTime.now().isAfter(usuario.getTokenExpiracion())) {
            throw new BusinessException("El código ha expirado. Genera uno nuevo.");
        }
        
        ValidationUtils.validarPassword(passwordNueva);
        
        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        
        usuario.setTokenRecuperacion(null);
        usuario.setTokenExpiracion(null);
        usuario.setIntentosFallidos(0);
        usuario.setFechaUltimoIntento(null);
        
        usuarioRepository.save(usuario);
    }
    
    /**
     * Verificar bloqueo por intentos fallidos
     */
    private void verificarBloqueoPorIntentos(Usuario usuario) {
        if (usuario.getIntentosRecuperacion() >= MAX_INTENTOS) {
            LocalDateTime fechaUltimo = usuario.getFechaUltimoIntentoRecuperacion();
            
            if (fechaUltimo != null) {
                long minutosDesde = Duration.between(fechaUltimo, LocalDateTime.now()).toMinutes();
                
                if (minutosDesde < MINUTOS_BLOQUEO) {
                    long minutosRestantes = MINUTOS_BLOQUEO - minutosDesde;
                    throw new BusinessException(
                        "Demasiados intentos fallidos. " +
                        "Inténtalo nuevamente en " + minutosRestantes + " minutos."
                    );
                } else {
                    usuario.setIntentosRecuperacion(0);
                    usuario.setFechaUltimoIntentoRecuperacion(null);
                    usuarioRepository.save(usuario);
                }
            }
        }
    }
    
    /**
     * Generar token aleatorio de 8 caracteres
     */
    private String generarTokenAleatorio() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            token.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        
        return token.toString();
    }
}