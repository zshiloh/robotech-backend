package com.robotech.service;

import com.robotech.dto.UsuarioResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.*;
import com.robotech.repository.*;
import com.robotech.dto.ConfiguracionInicialRequest;
import com.robotech.dto.EditarPerfilUsuarioRequest;
import com.robotech.util.Constants;
import com.robotech.util.DateUtils;
import com.robotech.util.ValidationUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private UsuarioRolRepository usuarioRolRepository;
    
    @Autowired
    private CatRolRepository catRolRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    @Autowired
    private EnfrentamientoRepository enfrentamientoRepository;
    
    @Autowired
    private RobotService robotService;
    
    @Autowired
    private RankingService rankingService;
    
    @Autowired
    private PuntosClubHistoricoRepository puntosClubHistoricoRepository;
    
    @Autowired
    private CatCategoriaPesoRepository catCategoriaPesoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // ================================================================
    // MÉTODOS DE CATEGORÍAS DE PESO
    // ================================================================
    
    /**
     * Crear categoría de peso
     */
    @Transactional
    public Map<String, Object> crearCategoriaPeso(
            String nombre, 
            String descripcion, 
            Double pesoMinimo, 
            Double pesoMaximo, 
            Double dimensionMaxima) {
        
        // Verificar que no exista una categoría con el mismo nombre
        if (catCategoriaPesoRepository.existsByNombre(nombre)) {
            throw new BusinessException("Ya existe una categoría con el nombre '" + nombre + "'");
        }
        
        // Crear categoría
        CatCategoriaPeso categoria = new CatCategoriaPeso();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setPesoMinimo(pesoMinimo.intValue());
        categoria.setPesoMaximo(pesoMaximo.intValue());
        categoria.setDimensionMaxima(dimensionMaxima != null ? dimensionMaxima.intValue() : null);
        categoria.setActiva(true); // Por defecto activa
        categoria.setFechaCreacion(LocalDateTime.now());
        
        categoria = catCategoriaPesoRepository.save(categoria);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categoria);
        response.put("mensaje", "Categoría creada exitosamente");
        
        return response;
    }
    
    /**
     * Editar categoría de peso
     */
    @Transactional
    public Map<String, Object> editarCategoriaPeso(
            Integer id,
            String nombre, 
            String descripcion, 
            Double pesoMinimo, 
            Double pesoMaximo, 
            Double dimensionMaxima) {
        
        CatCategoriaPeso categoria = catCategoriaPesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", id));
        
        // Verificar que no exista otra categoría con el mismo nombre (excepto esta)
        if (!categoria.getNombre().equals(nombre) && 
            catCategoriaPesoRepository.existsByNombre(nombre)) {
            throw new BusinessException("Ya existe una categoría con el nombre '" + nombre + "'");
        }
        
        // Actualizar campos
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setPesoMinimo(pesoMinimo.intValue());
        categoria.setPesoMaximo(pesoMaximo.intValue());
        categoria.setDimensionMaxima(dimensionMaxima != null ? dimensionMaxima.intValue() : null);
        categoria.setFechaActualizacion(LocalDateTime.now());
        
        categoria = catCategoriaPesoRepository.save(categoria);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categoria);
        response.put("mensaje", "Categoría actualizada exitosamente");
        
        return response;
    }
    
    /**
     * Cambiar estado de categoría de peso (activar/desactivar)
     */
    @Transactional
    public Map<String, Object> cambiarEstadoCategoriaPeso(Integer id, Boolean activo) {
        CatCategoriaPeso categoria = catCategoriaPesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", id));
        
        categoria.setActiva(activo);
        categoria.setFechaActualizacion(LocalDateTime.now());
        
        categoria = catCategoriaPesoRepository.save(categoria);
        
        String accion = activo ? "activada" : "desactivada";
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categoria);
        response.put("mensaje", "Categoría " + accion + " exitosamente");
        
        return response;
    }
    
    /**
     * Eliminar categoría de peso (solo si NO está en uso en ningún torneo)
     */
    @Transactional
    public Map<String, Object> eliminarCategoriaPeso(Integer id) {
        CatCategoriaPeso categoria = catCategoriaPesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", id));
        
        // Verificar si está en uso en algún torneo
        long usosEnTorneos = categoriaRepository.countByCategoriaPeso_IdCategoriaPeso(id);
        
        if (usosEnTorneos > 0) {
            throw new BusinessException(
                "No se puede eliminar la categoría '" + categoria.getNombre() + "' " +
                "porque está siendo usada en " + usosEnTorneos + " torneo(s)"
            );
        }
        
        catCategoriaPesoRepository.delete(categoria);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("mensaje", "Categoría eliminada exitosamente");
        
        return response;
    }
    
    // ================================================================
    // RESTO DE MÉTODOS EXISTENTES
    // ================================================================
    
    /**
     * Salir de club voluntariamente (Competidor)
     */
    @Transactional
    public void salirDeClub(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        // Obtener robot del usuario
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new BusinessException("No tienes un robot creado"));
        
        // Verificar que tenga club
        if (robot.getClub() == null) {
            throw new BusinessException("No perteneces a ningún club");
        }
        
        Club club = robot.getClub();
        
        // No permitir salir si hay torneo activo
        if (torneoRepository.existeTorneoEnCurso(Constants.ESTADO_EN_CURSO)) {
            throw new BusinessException("No puedes salir del club mientras hay un torneo en curso");
        }
        
        // Verificar si el club está inactivo
        boolean clubInactivo = club.getEstadoActividad() == Club.EstadoActividad.INACTIVO || 
                               club.getEstadoActividad() == Club.EstadoActividad.INACTIVO_7D;
        
        // Si el club NO está inactivo, validar RST06
        if (!clubInactivo) {
            validarCambioClubTrimestral(usuario);
        }
        
        // Cerrar todos los registros históricos activos del usuario
        cerrarRegistrosHistoricos(idUsuario);
        
        // Marcar robot como inactivo
        robotService.marcarRobotInactivo(idUsuario, "Salió del club voluntariamente");
        
        // Quitar rol Competidor
        usuarioRolRepository.deleteByUsuarioIdAndRolId(idUsuario, Constants.ROL_COMPETIDOR);
        
        // Si el club NO está inactivo, incrementar contador de cambios
        if (!clubInactivo) {
            String trimestreActual = DateUtils.calcularTrimestreActual();
            
            if (!DateUtils.mismoPeriodoTrimestral(usuario.getUltimoTrimestreRegistrado(), trimestreActual)) {
                // Nuevo trimestre - resetear contador
                usuario.setCambiosClubTrimestreActual(1);
                usuario.setUltimoTrimestreRegistrado(trimestreActual);
            } else {
                // Mismo trimestre - incrementar
                usuario.setCambiosClubTrimestreActual(usuario.getCambiosClubTrimestreActual() + 1);
            }
            
            usuario.setCambiosClubTotalesHistorico(usuario.getCambiosClubTotalesHistorico() + 1);
            usuario.setFechaUltimoCambioClub(LocalDateTime.now());
            usuarioRepository.save(usuario);
        }
        
        // Recalcular ranking de club (ya no suma los puntos de este competidor)
        rankingService.recalcularRankingClubParaTodasLasCategorias(club.getIdClub());
    }
    
    /**
     * Expulsar competidor (Representante)
     */
    @Transactional
    public void expulsarCompetidor(Integer clubId, Integer idRepresentante, Integer idCompetidor) {
        // Verificar que el club existe
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", clubId));
        
        // Verificar que sea el representante
        if (!club.getRepresentante().getIdUsuario().equals(idRepresentante)) {
            throw new BusinessException("No tienes permisos para expulsar competidores de este club");
        }
        
        // Verificar que no se expulse a sí mismo
        if (idRepresentante.equals(idCompetidor)) {
            throw new BusinessException("No puedes expulsarte a ti mismo del club");
        }
        
        // No permitir expulsión si hay torneo activo
        if (torneoRepository.existeTorneoEnCurso(Constants.ESTADO_EN_CURSO)) {
            throw new BusinessException("No puedes expulsar competidores mientras hay un torneo en curso");
        }
        
        Usuario competidor = usuarioRepository.findById(idCompetidor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idCompetidor));
        
        // Verificar que el competidor pertenezca al club
        Robot robot = robotRepository.findByUsuario_IdUsuario(idCompetidor)
                .orElseThrow(() -> new BusinessException("El usuario no tiene un robot"));
        
        if (robot.getClub() == null || !robot.getClub().getIdClub().equals(clubId)) {
            throw new BusinessException("Este competidor no pertenece a tu club");
        }
        
        // Cerrar todos los registros históricos activos del competidor
        cerrarRegistrosHistoricos(idCompetidor);
        
        // Marcar robot como inactivo
        robotService.marcarRobotInactivo(idCompetidor, "Expulsado por el representante");
        
        // Quitar rol Competidor
        usuarioRolRepository.deleteByUsuarioIdAndRolId(idCompetidor, Constants.ROL_COMPETIDOR);
        
        // NO incrementar contador de cambios (no fue su decisión)
        
        // Recalcular ranking de club
        rankingService.recalcularRankingClubParaTodasLasCategorias(clubId);
        
        // Notificar al expulsado
        notificacionService.crearNotificacion(
            idCompetidor,
            Constants.NOTIF_EXPULSADO_CLUB,
            "Expulsado del club",
            "Has sido expulsado del club '" + club.getNombreClub() + "' por el representante."
        );
    }
    
    /**
     * Obtener información del usuario actual
     */
    public UsuarioResponse obtenerMiInformacion(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        return convertirAResponse(usuario);
    }
    
    /**
     * Asignar rol a usuario (Admin)
     */
    @Transactional
    public void asignarRol(Integer idUsuario, Integer idRol, Integer idAdmin) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        // Verificar que no se asigne a sí mismo (seguridad)
        if (idUsuario.equals(idAdmin)) {
            throw new BusinessException("No puedes modificar tus propios roles");
        }
        
        // Verificar que el rol no esté asignado ya
        if (usuarioRolRepository.existsByUsuarioIdAndRolId(idUsuario, idRol)) {
            throw new BusinessException("El usuario ya tiene este rol asignado");
        }
        
        // Asignar rol
        UsuarioRol.UsuarioRolId usuarioRolId = new UsuarioRol.UsuarioRolId(idUsuario, idRol);
        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setId(usuarioRolId);
        usuarioRolRepository.save(usuarioRol);
    }
    
    /**
     * Quitar rol a usuario (Admin)
     */
    @Transactional
    public void quitarRol(Integer idUsuario, Integer idRol, Integer idAdmin) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        // Verificar que no se quite a sí mismo (seguridad)
        if (idUsuario.equals(idAdmin)) {
            throw new BusinessException("No puedes modificar tus propios roles");
        }
        
        // Verificar que no sea el rol Usuario (base)
        if (idRol.equals(Constants.ROL_USUARIO)) {
            throw new BusinessException("No se puede quitar el rol de Usuario");
        }
        
        // Verificar que tenga el rol
        if (!usuarioRolRepository.existsByUsuarioIdAndRolId(idUsuario, idRol)) {
            throw new BusinessException("El usuario no tiene este rol asignado");
        }
        
        // Quitar rol
        usuarioRolRepository.deleteByUsuarioIdAndRolId(idUsuario, idRol);
    }
    
    /**
     * Obtener todos los usuarios (Admin)
     */
    public List<UsuarioResponse> obtenerTodosLosUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        return usuarios.stream()
                .map(this::convertirAResponse)
                .toList();
    }
    
    /**
     * Obtener cantidad total de usuarios (Admin)
     */
    public long obtenerTotalUsuarios() {
        return usuarioRepository.count();
    }
    
    /**
     * Validar al salir de club
     */
    private void validarCambioClubTrimestral(Usuario usuario) {
        String trimestreActual = DateUtils.calcularTrimestreActual();
        
        // Si cambió de trimestre, permitir
        if (!DateUtils.mismoPeriodoTrimestral(usuario.getUltimoTrimestreRegistrado(), trimestreActual)) {
            return;
        }
        
        // Verificar límite de 5 cambios por trimestre
        if (usuario.getCambiosClubTrimestreActual() >= Constants.MAX_CAMBIOS_CLUB_TRIMESTRE) {
            throw new BusinessException(
                "Has alcanzado el límite de " + Constants.MAX_CAMBIOS_CLUB_TRIMESTRE + 
                " cambios de club en este trimestre. Podrás cambiar nuevamente en el próximo trimestre."
            );
        }
        
        // Verificar espera de 7 días entre cambios
        if (usuario.getFechaUltimoCambioClub() != null) {
            long diasDesdeUltimoCambio = DateUtils.diasEntre(
                usuario.getFechaUltimoCambioClub(), 
                LocalDateTime.now()
            );
            
            if (diasDesdeUltimoCambio < Constants.DIAS_ESPERA_ENTRE_CAMBIOS) {
                long diasRestantes = Constants.DIAS_ESPERA_ENTRE_CAMBIOS - diasDesdeUltimoCambio;
                LocalDateTime proximaFecha = usuario.getFechaUltimoCambioClub()
                        .plusDays(Constants.DIAS_ESPERA_ENTRE_CAMBIOS);
                
                throw new BusinessException(
                    "Debes esperar " + Constants.DIAS_ESPERA_ENTRE_CAMBIOS + " días entre cada cambio de club. " +
                    "Podrás cambiar nuevamente el " + proximaFecha.toLocalDate()
                );
            }
        }
    }
    
    /**
     * Cerrar todos los registros históricos activos cuando un competidor sale del club
     */
    private void cerrarRegistrosHistoricos(Integer idUsuario) {
        // Obtener todos los registros activos del usuario
        List<PuntosClubHistorico> registrosActivos = 
            puntosClubHistoricoRepository.findAllActivosByUsuario(idUsuario);
        
        // Cerrar cada registro (marcar fecha_fin)
        for (PuntosClubHistorico registro : registrosActivos) {
            registro.cerrar(); // Método helper que marca fecha_fin = now()
            puntosClubHistoricoRepository.save(registro);
        }
    }
    
    /**
     * Actualizar perfil del usuario
     * Permite editar: nombreCompleto, apodoPersonal, preguntaSeguridad, respuestaSeguridad
     */
    @Transactional
    public UsuarioResponse actualizarPerfil(Integer idUsuario, EditarPerfilUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        // Actualizar nombre completo (obligatorio)
        if (request.getNombreCompleto() != null && !request.getNombreCompleto().trim().isEmpty()) {
            usuario.setNombreCompleto(request.getNombreCompleto().trim());
        }
        
        // Actualizar apodo personal (opcional, pero debe ser único si se proporciona)
        if (request.getApodoPersonal() != null && !request.getApodoPersonal().trim().isEmpty()) {
            String apodoNuevo = request.getApodoPersonal().trim();
            
            // Verificar que el apodo no esté en uso por otro usuario
            if (!apodoNuevo.equals(usuario.getApodoPersonal()) && 
                usuarioRepository.existsByApodoPersonal(apodoNuevo)) {
                throw new BusinessException("El apodo '" + apodoNuevo + "' ya está en uso");
            }
            
            usuario.setApodoPersonal(apodoNuevo);
        }
        
        // Actualizar pregunta de seguridad (opcional)
        if (request.getPreguntaSeguridad() != null && !request.getPreguntaSeguridad().trim().isEmpty()) {
            usuario.setPreguntaSeguridad(request.getPreguntaSeguridad().trim());
            
            // Si cambia la pregunta, DEBE cambiar la respuesta también
            if (request.getRespuestaSeguridad() == null || request.getRespuestaSeguridad().trim().isEmpty()) {
                throw new BusinessException("Si cambias la pregunta de seguridad, debes proporcionar una respuesta");
            }
        }
        
        // Actualizar respuesta de seguridad (opcional, solo si hay pregunta)
        if (request.getRespuestaSeguridad() != null && !request.getRespuestaSeguridad().trim().isEmpty()) {
            // Hashear la nueva respuesta
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            usuario.setRespuestaSeguridadHash(
                encoder.encode(request.getRespuestaSeguridad().toLowerCase().trim())
            );
        }
        
        usuarioRepository.save(usuario);
        
        return convertirAResponse(usuario);
    }

    /**
     * Actualizar todos los roles de un usuario (Admin)
     * Valida que no se quiten roles con dependencias activas
     */
    @Transactional
    public void actualizarRoles(Integer idUsuario, List<String> nombresRoles) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        // ⭐ Obtener el usuario actual (quien está haciendo el cambio)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailActual = auth.getName();
        Usuario usuarioActual = usuarioRepository.findByEmail(emailActual).orElse(null);
        
        // ⭐ SEGURIDAD: NO permitir que un admin se quite su propio rol Administrador
        if (usuarioActual != null && usuarioActual.getIdUsuario().equals(idUsuario)) {
            // Es el mismo usuario - verificar que no se quite Administrador
            boolean tieneAdminActualmente = usuario.getRoles().stream()
                    .anyMatch(rol -> rol.getNombreRol().equals("Administrador"));
            
            boolean mantendraAdmin = nombresRoles != null && nombresRoles.contains("Administrador");
            
            if (tieneAdminActualmente && !mantendraAdmin) {
                throw new BusinessException(
                    "No puedes quitarte el rol de Administrador a ti mismo. " +
                    "Pide a otro administrador que lo haga."
                );
            }
        }
        
        // Si nombresRoles es null, inicializar lista vacía
        if (nombresRoles == null) {
            nombresRoles = new ArrayList<>();
        }
        
        // ⭐ OBTENER ROLES ACTUALES (SIN incluir "Usuario" - es automático)
        Set<String> rolesActuales = usuario.getRoles().stream()
                .map(CatRol::getNombreRol)
                .filter(rol -> !rol.equals("Usuario"))  // ⭐ Ignorar Usuario en la comparación
                .collect(Collectors.toSet());
        
        // ⭐ OBTENER ROLES NUEVOS (SIN incluir "Usuario" - es automático)
        Set<String> rolesNuevos = new HashSet<>(nombresRoles);
        rolesNuevos.remove("Usuario");  // ⭐ Ignorar Usuario si viene en la lista
        
        // ⭐ DETECTAR ROLES QUE SE VAN A QUITAR (ahora sin Usuario)
        Set<String> rolesAQuitar = new HashSet<>(rolesActuales);
        rolesAQuitar.removeAll(rolesNuevos);
        
        // ⭐ VALIDAR QUE NO SE QUITEN ROLES CON DEPENDENCIAS
        for (String rolAQuitar : rolesAQuitar) {
            validarQuitarRol(usuario, rolAQuitar);
        }
        
        // Limpiar todos los roles actuales
        usuario.getRoles().clear();
        
        // SIEMPRE agregar el rol "Usuario"
        CatRol rolUsuario = catRolRepository.findByNombreRol("Usuario")
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", "Usuario"));
        usuario.getRoles().add(rolUsuario);
        
        // Agregar los nuevos roles seleccionados (si no es "Usuario" otra vez)
        for (String nombreRol : rolesNuevos) {
            if (!nombreRol.equals("Usuario")) {
                CatRol rol = catRolRepository.findByNombreRol(nombreRol)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol", "nombre", nombreRol));
                usuario.getRoles().add(rol);
            }
        }
        
        // Guardar cambios
        usuarioRepository.save(usuario);
        
        // Notificar al usuario
        notificacionService.crearNotificacion(
            idUsuario,
            "ROLES_ACTUALIZADOS",
            "Roles actualizados",
            "Tus roles han sido actualizados por un administrador."
        );
    }
    
    /**
     * Validar que se puede quitar un rol específico
     */
    private void validarQuitarRol(Usuario usuario, String nombreRol) {
        Integer idUsuario = usuario.getIdUsuario();
        
        switch (nombreRol) {
            case "Jurado":
                // ⭐ NO permitir quitar si registró combates (PERMANENTE)
                if (enfrentamientoRepository.existsByJuradoRegistro_IdUsuario(idUsuario)) {
                    long combates = enfrentamientoRepository.countByJuradoRegistro_IdUsuario(idUsuario);
                    throw new BusinessException(
                        "No se puede quitar el rol de Jurado. " +
                        "Este usuario registró resultados de " + combates + " combate(s). " +
                        "El historial de arbitraje es permanente y debe preservarse."
                    );
                }
                // ✅ SI no tiene combates registrados, se puede quitar
                break;
                
            case "Representante":
                // ⭐ NO permitir quitar si tiene un club (cualquier estado)
                if (clubRepository.existsByRepresentante_IdUsuario(idUsuario)) {
                    List<Club> clubes = clubRepository.findByRepresentante_IdUsuario(idUsuario);
                    if (clubes != null && !clubes.isEmpty()) {
                        Club club = clubes.get(0);
                        String estadoClub = club.getEstado().getDescripcion();
                        throw new BusinessException(
                            "No se puede quitar el rol de Representante. " +
                            "Este usuario es representante del club '" + club.getNombreClub() + 
                            "' (estado: " + estadoClub + "). " +
                            "El representante debe eliminar el club primero."
                        );
                    }
                }
                // ✅ SI no tiene club, se puede quitar
                break;
                
            case "Competidor":
                // ⭐ NO permitir quitar si tiene robot ACTIVO (en un club)
                if (robotRepository.existsByUsuario_IdUsuario(idUsuario)) {
                    Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario).orElse(null);
                    if (robot != null && robot.getEstado() == Robot.EstadoRobot.ACTIVO) {
                        String nombreClub = robot.getClub() != null ? robot.getClub().getNombreClub() : "desconocido";
                        throw new BusinessException(
                            "No se puede quitar el rol de Competidor. " +
                            "Este usuario tiene un robot activo ('" + robot.getNombreRobot() + "') " +
                            "en el club '" + nombreClub + "'. " +
                            "El competidor debe salir del club primero."
                        );
                    }
                }
                // ✅ SI no tiene robot activo (está fuera de club), se puede quitar
                // NOTA: El historial de puntos se mantiene, pero el rol se puede quitar
                break;
                
            case "Organizador":
                // ✅ SIEMPRE se puede quitar
                // Los torneos creados son del sistema, no del organizador
                break;
                
            case "Administrador":
                // ✅ Se puede quitar (si no es a sí mismo - ya validado arriba)
                break;
                
            case "Usuario":
                // ❌ NUNCA se puede quitar el rol Usuario (es el rol base)
                // PERO este caso nunca debería llegar aquí porque lo filtramos arriba
                throw new BusinessException("El rol de Usuario no se puede quitar");
        }
    }
    
    /**
     * Cambiar contraseña
     */
    @Transactional
    public void cambiarPassword(Integer idUsuario, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        // Verificar contraseña actual
        org.springframework.security.crypto.password.PasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        if (!encoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new BusinessException("La contraseña actual es incorrecta");
        }
        
        // Validar que la nueva contraseña sea diferente
        if (passwordActual.equals(passwordNueva)) {
            throw new BusinessException("La nueva contraseña debe ser diferente a la actual");
        }
        
        // Hashear y guardar nueva contraseña
        usuario.setPasswordHash(encoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        
        // Notificar al usuario
        notificacionService.crearNotificacion(
            idUsuario,
            Constants.NOTIF_PASSWORD_CAMBIADA,
            "Contraseña actualizada",
            "Tu contraseña ha sido cambiada exitosamente."
        );
    }
    
    /**
	 * Completar configuración inicial del usuario
	 */
    @Transactional
    public void configuracionInicial(String email, ConfiguracionInicialRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        
        // Validar que las contraseñas coincidan
        if (!request.getPasswordNueva().equals(request.getConfirmarPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }
        
        // Validar fortaleza
        ValidationUtils.validarPassword(request.getPasswordNueva());
        
        // Cambiar contraseña
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setPasswordHash(encoder.encode(request.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);  // Marcar como completado
        
        // ⭐ Actualizar nombre completo (OPCIONAL - si no lo proporciona, queda "Usuario Nuevo")
        if (request.getNombreCompleto() != null && !request.getNombreCompleto().trim().isEmpty()) {
            ValidationUtils.validarNombreCompleto(request.getNombreCompleto());
            usuario.setNombreCompleto(request.getNombreCompleto().trim());
        }
        // Si no proporciona nombre, queda como "Usuario Nuevo" (valor por defecto del admin)
        
        // ⭐ Validar teléfono (OBLIGATORIO)
        if (request.getTelefono() == null || request.getTelefono().trim().isEmpty()) {
            throw new BusinessException("El teléfono es obligatorio para contacto");
        }
        
        String telefonoFormateado = ValidationUtils.formatearTelefono(request.getTelefono());
        
        // Verificar que no esté en uso
        if (usuarioRepository.existsByTelefono(telefonoFormateado)) {
            throw new BusinessException("El teléfono ya está registrado");
        }
        
        usuario.setTelefono(telefonoFormateado);
        
        // Actualizar apodo (OPCIONAL)
        if (request.getApodoPersonal() != null && !request.getApodoPersonal().trim().isEmpty()) {
            String apodo = request.getApodoPersonal().trim();
            
            // Verificar que no esté en uso
            if (usuarioRepository.existsByApodoPersonal(apodo)) {
                throw new BusinessException("El apodo ya está en uso");
            }
            
            usuario.setApodoPersonal(apodo);
        }
        
        usuarioRepository.save(usuario);
    }
    
    /**
     * Crear usuario por parte del admin
     */
    @Transactional
    public Map<String, Object> crearUsuarioPorAdmin(String email) {
        // Validar email
        ValidationUtils.validarEmail(email);
        
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("El email ya está registrado");
        }
        
        // Generar contraseña temporal
        String passwordTemporal = generarPasswordTemporal();
        
        // Asignar rol Usuario ANTES de crear el usuario
        CatRol rolUsuario = catRolRepository.findById(Constants.ROL_USUARIO)
                .orElseThrow(() -> new ResourceNotFoundException("Rol Usuario no encontrado"));
        
        // Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Usuario Nuevo");
        usuario.setEmail(email);
        usuario.setTelefono(null);
        usuario.setPasswordHash(passwordEncoder.encode(passwordTemporal));
        usuario.setDebeCambiarPassword(true);
        usuario.setUltimoTrimestreRegistrado(DateUtils.calcularTrimestreActual());
        
        // No ha iniciado sesión todavía
        usuario.setUltimoInicioSesion(null);
        
        // Agregar rol ANTES de guardar
        usuario.getRoles().add(rolUsuario);
        
        // Guardar una sola vez
        usuario = usuarioRepository.save(usuario);
        
        return Map.of(
            "passwordTemporal", passwordTemporal,
            "email", email
        );
    }

    private String generarPasswordTemporal() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        String special = "!@#$%^&*";
        Random random = new Random();
        
        StringBuilder password = new StringBuilder("Robotech2025");
        
        for (int i = 0; i < 4; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        password.append(special.charAt(random.nextInt(special.length())));
        
        return password.toString();
    }
    
    /**
     * Convertir entidad a DTO
     */
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(CatRol::getNombreRol)
                .toList();
        
        // Obtener nombre del club si es competidor
        String nombreClub = null;
        try {
            Robot robot = robotRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .orElse(null);
            
            if (robot != null && robot.getClub() != null) {
                nombreClub = robot.getClub().getNombreClub();
            }
        } catch (Exception e) {
            // Si no tiene robot o club, nombreClub queda como null
        }
        
     // Determinar si el usuario está activo
        Boolean activo = false;
        if (usuario.getUltimoInicioSesion() != null) {
            LocalDateTime haceDiasInactividad = LocalDateTime.now().minusDays(Constants.DIAS_INACTIVIDAD_USUARIO);
            activo = usuario.getUltimoInicioSesion().isAfter(haceDiasInactividad);
        }
        
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
            nombreClub,
            activo
        );
    }
}