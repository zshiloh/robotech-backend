package com.robotech.repository;

import com.robotech.model.Enfrentamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnfrentamientoRepository extends JpaRepository<Enfrentamiento, Integer> {
    
    List<Enfrentamiento> findByCategoria_IdCategoria(Integer idCategoria);
    
    long countByCategoria_IdCategoria(Integer idCategoria);
    
    long countByCategoria_IdCategoriaAndEstadoCombate(Integer idCategoria, String estadoCombate);
    
    @Query("SELECT e FROM Enfrentamiento e " +
           "WHERE e.categoria.idCategoria = :categoriaId " +
           "AND e.estadoCombate = 'Pendiente'")
    List<Enfrentamiento> findEnfrentamientosPendientesByCategoria(
        @Param("categoriaId") Integer categoriaId
    );
    
    @Query("SELECT e FROM Enfrentamiento e " +
           "WHERE (e.competidor1.idUsuario = :usuarioId " +
           "OR e.competidor2.idUsuario = :usuarioId) " +
           "AND e.estadoCombate = 'Finalizado'")
    List<Enfrentamiento> findEnfrentamientosFinalizadosByUsuario(
        @Param("usuarioId") Integer usuarioId
    );
    
    @Query("SELECT COUNT(e) FROM Enfrentamiento e " +
           "WHERE e.ganador.idUsuario = :usuarioId " +
           "AND e.categoria.idCategoria = :categoriaId " +
           "AND e.estadoCombate = 'Finalizado'")
    int countVictoriasByUsuarioAndCategoria(
        @Param("usuarioId") Integer usuarioId,
        @Param("categoriaId") Integer categoriaId
    );
    
    List<Enfrentamiento> findByCategoria_IdCategoriaAndFase_IdFaseOrderByIdEnfrentamientoAsc(
        Integer idCategoria, Integer idFase);
    
    @Query("SELECT e FROM Enfrentamiento e WHERE e.categoria.idCategoria = :idCategoria " +
           "AND e.competidor1 IS NOT NULL AND e.competidor2 IS NOT NULL " +
           "AND e.estadoCombate = 'Pendiente'")
    List<Enfrentamiento> findEnfrentamientosListosParaJuez(@Param("idCategoria") Integer idCategoria);
    
    long countByEstadoCombate(String estadoCombate);
    
    boolean existsByJuradoRegistro_IdUsuario(Integer idUsuario);
    
    long countByJuradoRegistro_IdUsuario(Integer idUsuario);
}