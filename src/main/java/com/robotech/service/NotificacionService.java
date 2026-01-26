package com.robotech.service;

import com.robotech.dto.NotificacionResponse;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.Notificacion;
import com.robotech.model.Usuario;
import com.robotech.repository.NotificacionRepository;
import com.robotech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificacionService {
    
    @Autowired
    private NotificacionRepository notificacionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    /**
     * Crear una notificación para un usuario
     */
    @Transactional
    public void crearNotificacion(Integer idUsuario, String tipo, String titulo, String mensaje) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(tipo);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setLeida(false);
        
        notificacionRepository.save(notificacion);
    }
    
    /**
     * Obtener todas las notificaciones de un usuario
     */
    public List<NotificacionResponse> obtenerNotificaciones(Integer idUsuario, Boolean leidas, Integer limit) {
        List<Notificacion> notificaciones;
        
        if (leidas == null) {
            notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaDesc(idUsuario);
        } else if (leidas) {
            notificaciones = notificacionRepository.findNotificacionesLeidas(idUsuario);
        } else {
            notificaciones = notificacionRepository.findNotificacionesNoLeidas(idUsuario);
        }
        
        if (limit != null && limit > 0) {
            notificaciones = notificaciones.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        return notificaciones.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Contar notificaciones no leídas
     */
    public long contarNoLeidas(Integer idUsuario) {
        return notificacionRepository.countNotificacionesNoLeidas(idUsuario);
    }
    
    /**
     * Marcar una notificación como leída
     */
    @Transactional
    public void marcarComoLeida(Integer idNotificacion, Integer idUsuario) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", idNotificacion));
        
        if (!notificacion.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResourceNotFoundException("Notificación no encontrada para este usuario");
        }
        
        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }
    
    /**
     * Marcar todas las notificaciones como leídas
     */
    @Transactional
    public void marcarTodasComoLeidas(Integer idUsuario) {
        notificacionRepository.marcarTodasComoLeidas(idUsuario);
    }
    
    /**
     * Convertir entidad a DTO
     */
    private NotificacionResponse convertirAResponse(Notificacion notificacion) {
        return new NotificacionResponse(
            notificacion.getIdNotificacion(),
            notificacion.getTipo(),
            notificacion.getTitulo(),
            notificacion.getMensaje(),
            notificacion.getLeida(),
            notificacion.getFechaCreacion()
        );
    }
}