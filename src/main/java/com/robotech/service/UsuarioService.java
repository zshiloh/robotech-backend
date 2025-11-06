package com.robotech.service;

import com.robotech.model.Rol;
import com.robotech.model.Usuario;
import com.robotech.repository.RolRepository;
import com.robotech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * CU14: Iniciar Sesión
     * Busca usuario por correo y valida contraseña
     */
    public Optional<Usuario> autenticar(String correo, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByCorreo(correo);
        
        if (usuario.isPresent() && usuario.get().getActivo()) {
            // Verificar contraseña hasheada
            if (passwordEncoder.matches(password, usuario.get().getPassword())) {
                // Actualizar último acceso
                usuario.get().setUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(usuario.get());
                return usuario;
            }
        }
        return Optional.empty();
    }

    /**
     * CU15: Gestionar Roles
     * Actualiza el rol de un usuario
     */
    public Usuario actualizarRol(Integer idUsuario, Integer idRol) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    /**
     * Listar todos los usuarios
     */
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Listar usuarios activos
     */
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Buscar usuario por correo
     */
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    /**
     * Crear nuevo usuario
     */
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el correo no exista
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
        
        // Validar que el teléfono no exista
        if (usuarioRepository.existsByTelefono(usuario.getTelefono())) {
            throw new RuntimeException("El teléfono ya está registrado");
        }
        
        // Verificar que el password no sea null
        if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }
        
        // Hashear contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        return usuarioRepository.save(usuario);
    }

    /**
     * Activar/Desactivar usuario
     */
    public Usuario cambiarEstado(Integer idUsuario, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setActivo(activo);
        return usuarioRepository.save(usuario);
    }
}