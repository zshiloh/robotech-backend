package com.robotech.repository;

import com.robotech.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    
    @Query("SELECT n FROM Notificacion n " +
           "WHERE n.usuario.idUsuario = :usuarioId " +
           "ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(@Param("usuarioId") Integer usuarioId);
    
    @Query("SELECT n FROM Notificacion n " +
           "WHERE n.usuario.idUsuario = :usuarioId " +
           "AND n.leida = false " +
           "ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNotificacionesNoLeidas(@Param("usuarioId") Integer usuarioId);
    
    @Query("SELECT COUNT(n) FROM Notificacion n " +
           "WHERE n.usuario.idUsuario = :usuarioId " +
           "AND n.leida = false")
    int countNotificacionesNoLeidas(@Param("usuarioId") Integer usuarioId);
    
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true " +
           "WHERE n.usuario.idUsuario = :usuarioId " +
           "AND n.leida = false")
    void marcarTodasComoLeidas(@Param("usuarioId") Integer usuarioId);
    
    @Query("SELECT n FROM Notificacion n " +
           "WHERE n.usuario.idUsuario = :usuarioId " +
           "AND n.leida = true " +
           "ORDER BY n.fechaCreacion DESC")
    List<Notificacion> findNotificacionesLeidas(@Param("usuarioId") Integer usuarioId);

    boolean existsByUsuario_IdUsuario(Integer idUsuario);
    
    long countByUsuario_IdUsuario(Integer idUsuario);
    
    void deleteByUsuario_IdUsuario(Integer idUsuario);
}