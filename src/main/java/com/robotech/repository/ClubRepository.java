package com.robotech.repository;

import com.robotech.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Integer> {
    
    boolean existsByNombreClub(String nombreClub);
    
    Optional<Club> findByNombreClub(String nombreClub);

    @Query("SELECT c FROM Club c JOIN FETCH c.representante JOIN FETCH c.estado WHERE c.estado.idEstado = :estadoId")
    List<Club> findByEstadoId(@Param("estadoId") Integer estadoId);

    @Query("SELECT c FROM Club c JOIN FETCH c.representante JOIN FETCH c.estado WHERE c.representante.idUsuario = :idRepresentante")
    List<Club> findByRepresentante_IdUsuario(@Param("idRepresentante") Integer idRepresentante);

    @Query("SELECT c FROM Club c " +
           "WHERE c.representante.ultimoInicioSesion < :fechaLimite " +
           "AND c.estadoActividad != 'INACTIVO'")
    List<Club> findClubesConRepresentanteInactivo(@Param("fechaLimite") LocalDateTime fechaLimite);
    
    @Query("SELECT c FROM Club c " +
           "WHERE c.estado.idEstado = :estadoValidado " +
           "AND c.estadoActividad = 'ACTIVO' " +
           "ORDER BY c.nombreClub ASC")
    List<Club> findClubesPublicos(@Param("estadoValidado") Integer estadoValidado);
    
    @Query("SELECT c FROM Club c " +
           "WHERE c.estado.idEstado = :estadoValidado " +
           "AND c.estadoActividad = 'ACTIVO' " +
           "AND LOWER(c.nombreClub) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Club> searchClubesPublicos(
        @Param("estadoValidado") Integer estadoValidado,
        @Param("search") String search
    );
    
    @Query("SELECT c FROM Club c JOIN FETCH c.representante JOIN FETCH c.estado " +
           "ORDER BY c.fechaCreacion DESC")
    List<Club> findAllClubesConEstado();
    
    boolean existsByRepresentante_IdUsuario(Integer idUsuario);
}