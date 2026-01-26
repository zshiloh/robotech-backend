package com.robotech.repository;

import com.robotech.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Integer> {
    
    @Query("SELECT t FROM Torneo t WHERE t.estado.idEstado = :estadoId ORDER BY t.fechaInicio DESC")
    List<Torneo> findByEstadoId(@Param("estadoId") Integer estadoId);
    
    @Query("SELECT COUNT(t) > 0 FROM Torneo t WHERE t.estado.idEstado = :estadoEnCurso")
    boolean existeTorneoEnCurso(@Param("estadoEnCurso") Integer estadoEnCurso);
    
    @Query("SELECT t FROM Torneo t WHERE t.estado.idEstado = :estadoEnCurso")
    Optional<Torneo> findTorneoEnCurso(@Param("estadoEnCurso") Integer estadoEnCurso);
    
    @Query("SELECT t FROM Torneo t ORDER BY t.fechaInicio DESC")
    List<Torneo> findAllOrderByFechaDesc();
    
    boolean existsBySede_IdSede(Integer idSede);
}