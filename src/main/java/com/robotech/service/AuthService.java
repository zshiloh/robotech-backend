package com.robotech.service;

import com.robotech.dto.LoginRequest;
import com.robotech.dto.LoginResponse;
import com.robotech.dto.RegistroConClubRequest;
import com.robotech.dto.RegistroRequest;
import com.robotech.dto.UsuarioResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.exception.UnauthorizedException;
import com.robotech.exception.ValidationException;
import com.robotech.model.*;
import com.robotech.repository.*;
import com.robotech.security.JwtUtil;
import com.robotech.util.Constants;
import com.robotech.util.DateUtils;
import com.robotech.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private CatRolRepository catRolRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Login de usuario
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email o contraseña incorrectos"));
        
        verificarBloqueoPorIntentos(usuario);
        
        try {
        	
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            
            usuario.setIntentosFallidos(0);
            usuario.setFechaUltimoIntento(null);
            usuario.setUltimoInicioSesion(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            String token = jwtUtil.generateToken(authentication);
            
            List<String> roles = usuario.getRoles().stream()
                    .map(CatRol::getNombreRol)
                    .collect(Collectors.toList());
            
            String apodo = usuario.getApodoPersonal();
            
            String nombreClub = null;
            try {
                Robot robot = robotRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                        .orElse(null);
                
                if (robot != null && robot.getClub() != null) {
                    nombreClub = robot.getClub().getNombreClub();
                }
            } catch (Exception e) {
            	
            }

            Boolean activo = false;
            if (usuario.getUltimoInicioSesion() != null) {
                LocalDateTime hace90Dias = LocalDateTime.now().minusDays(90);
                activo = usuario.getUltimoInicioSesion().isAfter(hace90Dias);
            }

            UsuarioResponse usuarioResponse = new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getFechaRegistro(),
                usuario.getUltimoInicioSesion(),
                roles,
                usuario.getApodoPersonal(),
                usuario.getPreguntaSeguridad(),
                nombreClub,
                activo
            );

            return new LoginResponse(token, usuarioResponse, apodo, roles, usuario.getDebeCambiarPassword());
            
        } catch (BadCredentialsException ex) {
        	
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            usuario.setFechaUltimoIntento(LocalDateTime.now());
            usuarioRepository.save(usuario);
            
            int intentosRestantes = Constants.MAX_INTENTOS_LOGIN - usuario.getIntentosFallidos();
            
            if (intentosRestantes > 0) {
                throw new UnauthorizedException(
                    "Email o contraseña incorrectos. Intentos restantes: " + intentosRestantes
                );
            } else {
                throw new UnauthorizedException(
                    "Cuenta bloqueada por 15 minutos debido a múltiples intentos fallidos"
                );
            }
        }
    }
    
    /**
     * Registro de usuario
     */
    @Transactional
    public UsuarioResponse registro(RegistroRequest request) {
        
        if (!request.getPassword().equals(request.getConfirmarPassword())) {
            throw new ValidationException("Las contraseñas no coinciden");
        }
        
        ValidationUtils.validarEmail(request.getEmail());
        ValidationUtils.validarPassword(request.getPassword());
        ValidationUtils.validarNombreCompleto(request.getNombreCompleto());
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }
        
        String telefonoFormateado = null;
        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            telefonoFormateado = ValidationUtils.formatearTelefono(request.getTelefono());
            
            if (usuarioRepository.existsByTelefono(telefonoFormateado)) {
                throw new BusinessException("El teléfono ya está registrado");
            }
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(telefonoFormateado);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setUltimoTrimestreRegistrado(DateUtils.calcularTrimestreActual());
        usuario.setPreguntaSeguridad(request.getPreguntaSeguridad());
        
        // ✅ CORREGIDO: Solo hashear respuesta si NO es null
        if (request.getRespuestaSeguridad() != null && !request.getRespuestaSeguridad().trim().isEmpty()) {
            usuario.setRespuestaSeguridadHash(
                passwordEncoder.encode(request.getRespuestaSeguridad().toLowerCase().trim())
            );
        } else {
            usuario.setRespuestaSeguridadHash(null);
        }
        
        usuario = usuarioRepository.save(usuario);
        
        CatRol rolUsuario = catRolRepository.findById(Constants.ROL_USUARIO)
                .orElseThrow(() -> new ResourceNotFoundException("Rol Usuario no encontrado"));
        
        usuario.getRoles().add(rolUsuario);
        usuarioRepository.save(usuario);
        
        List<String> roles = usuario.getRoles().stream()
                .map(CatRol::getNombreRol)
                .toList();

        Boolean activo = false;

        return new UsuarioResponse(
            usuario.getIdUsuario(),
            usuario.getNombreCompleto(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getFechaRegistro(),
            usuario.getUltimoInicioSesion(),
            roles,
            usuario.getApodoPersonal(),
            usuario.getPreguntaSeguridad(),
            null,
            activo
        );
    }
    
    /**
     * Registro de usuario con club
     */
    @Transactional
    public UsuarioResponse registroConClub(RegistroConClubRequest request) {
        
        if (!request.getPassword().equals(request.getConfirmarPassword())) {
            throw new ValidationException("Las contraseñas no coinciden");
        }
        
        ValidationUtils.validarEmail(request.getEmail());
        ValidationUtils.validarPassword(request.getPassword());
        ValidationUtils.validarNombreCompleto(request.getNombreCompleto());
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }
        
        if (clubRepository.existsByNombreClub(request.getNombreClub())) {
            throw new BusinessException("El nombre del club ya está en uso");
        }
        
        String telefonoFormateado = null;
        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            telefonoFormateado = ValidationUtils.formatearTelefono(request.getTelefono());
            
            if (usuarioRepository.existsByTelefono(telefonoFormateado)) {
                throw new BusinessException("El teléfono ya está registrado");
            }
        }
        
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setEmail(request.getEmail());
        usuario.setTelefono(telefonoFormateado);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setUltimoTrimestreRegistrado(DateUtils.calcularTrimestreActual());
        usuario.setPreguntaSeguridad(request.getPreguntaSeguridad());
        
        // ✅ CORREGIDO: Solo hashear respuesta si NO es null (LÍNEA 254)
        if (request.getRespuestaSeguridad() != null && !request.getRespuestaSeguridad().trim().isEmpty()) {
            usuario.setRespuestaSeguridadHash(
                passwordEncoder.encode(request.getRespuestaSeguridad().toLowerCase().trim())
            );
        } else {
            usuario.setRespuestaSeguridadHash(null);
        }

        usuario = usuarioRepository.save(usuario);
        
        CatRol rolUsuario = catRolRepository.findById(Constants.ROL_USUARIO)
                .orElseThrow(() -> new ResourceNotFoundException("Rol Usuario no encontrado"));
        
        usuario.getRoles().add(rolUsuario);
        usuarioRepository.save(usuario);
        
        CatEstado estadoPendiente = catEstadoRepository.findById(Constants.ESTADO_PENDIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Pendiente no encontrado"));
        
        Club club = new Club();
        club.setNombreClub(request.getNombreClub());
        club.setDescripcion(request.getDescripcionClub());
        club.setRepresentante(usuario);
        club.setEstado(estadoPendiente);
        clubRepository.save(club);
        
        CatRol rolAdmin = catRolRepository.findById(Constants.ROL_ADMINISTRADOR)
                .orElseThrow(() -> new ResourceNotFoundException("Rol Administrador no encontrado"));
        
        List<Usuario> administradores = usuarioRepository.findByRolId(Constants.ROL_ADMINISTRADOR);
        
        for (Usuario admin : administradores) {
            notificacionService.crearNotificacion(
                admin.getIdUsuario(),
                Constants.NOTIF_NUEVO_CLUB,
                "Nuevo club pendiente de validación",
                "El club '" + club.getNombreClub() + "' creado por " + usuario.getNombreCompleto() + 
                " está esperando validación."
            );
        }
        
        List<String> roles = usuario.getRoles().stream()
                .map(CatRol::getNombreRol)
                .toList();
        
        Boolean activo = false;

        return new UsuarioResponse(
            usuario.getIdUsuario(),
            usuario.getNombreCompleto(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.getFechaRegistro(),
            usuario.getUltimoInicioSesion(),
            roles,
            usuario.getApodoPersonal(),
            usuario.getPreguntaSeguridad(),
            club.getNombreClub(),
            activo
        );
    }
    
    /**
     * Verifica si el usuario está bloqueado por intentos fallidos
     */
    private void verificarBloqueoPorIntentos(Usuario usuario) {
        if (usuario.getIntentosFallidos() >= Constants.MAX_INTENTOS_LOGIN) {
            LocalDateTime fechaUltimoIntento = usuario.getFechaUltimoIntento();
            
            if (fechaUltimoIntento != null) {
                long minutosDesdeUltimoIntento = java.time.Duration
                        .between(fechaUltimoIntento, LocalDateTime.now())
                        .toMinutes();
                
                if (minutosDesdeUltimoIntento < Constants.MINUTOS_BLOQUEO_LOGIN) {
                    long minutosRestantes = Constants.MINUTOS_BLOQUEO_LOGIN - minutosDesdeUltimoIntento;
                    throw new UnauthorizedException(
                        "Cuenta bloqueada. Intenta nuevamente en " + minutosRestantes + " minutos."
                    );
                } else {
                    usuario.setIntentosFallidos(0);
                    usuario.setFechaUltimoIntento(null);
                    usuarioRepository.save(usuario);
                }
            }
        }
    }
}