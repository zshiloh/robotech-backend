package com.robotech.repository;

import com.robotech.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {
    
    @Query("SELECT s FROM Solicitud s " +
           "WHERE s.club.idClub = :clubId " +
           "AND s.estado.idEstado = :estadoPendiente")
    List<Solicitud> findSolicitudesPendientesByClubId(
        @Param("clubId") Integer clubId,
        @Param("estadoPendiente") Integer estadoPendiente
    );
    
    @Query("SELECT COUNT(s) > 0 FROM Solicitud s " +
           "WHERE s.usuario.idUsuario = :usuarioId " +
           "AND s.club.idClub = :clubId " +
           "AND s.estado.idEstado = :estadoPendiente")
    boolean existsSolicitudPendiente(
        @Param("usuarioId") Integer usuarioId,
        @Param("clubId") Integer clubId,
        @Param("estadoPendiente") Integer estadoPendiente
    );
    
    @Query("SELECT s FROM Solicitud s " +
    	       "WHERE s.usuario.idUsuario = :usuarioId " +
    	       "AND s.club.idClub = :clubId " +
    	       "AND s.estado.idEstado = :estadoRechazada " +
    	       "ORDER BY s.fechaRespuesta DESC")
    	Optional<Solicitud> findUltimaSolicitudRechazada(
    	    @Param("usuarioId") Integer usuarioId,
    	    @Param("clubId") Integer clubId,
    	    @Param("estadoRechazada") Integer estadoRechazada
    	);
    
    @Query("SELECT s FROM Solicitud s " +
    	       "WHERE s.club.idClub = :clubId " +
    	       "ORDER BY s.fechaSolicitud DESC")
    	List<Solicitud> findAllByClubId(@Param("clubId") Integer clubId);
    
    @Query("SELECT s FROM Solicitud s " +
           "WHERE s.usuario.idUsuario = :usuarioId " +
           "ORDER BY s.fechaSolicitud DESC")
    List<Solicitud> findByUsuario_IdUsuario(@Param("usuarioId") Integer usuarioId);
    
    Optional<Solicitud> findByIdSolicitudAndClub_IdClub(Integer idSolicitud, Integer clubId);
}