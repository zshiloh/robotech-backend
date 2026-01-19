package com.robotech.repository;

import com.robotech.model.Invitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitacionRepository extends JpaRepository<Invitacion, Integer> {
    
    Optional<Invitacion> findByToken(String token);
    
    @Query("SELECT i FROM Invitacion i " +
           "WHERE i.club.idClub = :clubId " +
           "AND i.estado.idEstado = :estadoPendiente")
    List<Invitacion> findInvitacionesPendientesByClubId(
        @Param("clubId") Integer clubId,
        @Param("estadoPendiente") Integer estadoPendiente
    );
    
    @Query("SELECT COUNT(i) FROM Invitacion i " +
           "WHERE i.club.idClub = :clubId " +
           "AND i.estado.idEstado = :estadoPendiente")
    int countInvitacionesPendientesByClubId(
        @Param("clubId") Integer clubId,
        @Param("estadoPendiente") Integer estadoPendiente
    );
    
    @Query("SELECT i FROM Invitacion i " +
           "WHERE i.emailDestinatario = :email " +
           "AND i.estado.idEstado = :estadoPendiente")
    List<Invitacion> findInvitacionesPendientesByEmail(
        @Param("email") String email,
        @Param("estadoPendiente") Integer estadoPendiente
    );
    
    @Query("SELECT i FROM Invitacion i " +
           "WHERE i.club.idClub = :clubId " +
           "ORDER BY i.fechaEnvio DESC")
    List<Invitacion> findAllInvitacionesByClubId(@Param("clubId") Integer clubId);
}