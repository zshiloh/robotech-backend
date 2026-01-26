package com.robotech.repository;

import com.robotech.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    
    // Categorías de un torneo
    List<Categoria> findByTorneo_IdTorneo(Integer idTorneo);
    
    // Categorías activas de un torneo
    @Query("SELECT c FROM Categoria c " +
           "WHERE c.torneo.idTorneo = :torneoId " +
           "AND c.estado.idEstado = :estadoActiva")
    List<Categoria> findCategoriasActivasByTorneoId(
        @Param("torneoId") Integer torneoId,
        @Param("estadoActiva") Integer estadoActiva
    );
    
    // Verificar si categoría tiene enfrentamientos
    @Query("SELECT c.tieneEnfrentamientos FROM Categoria c WHERE c.idCategoria = :categoriaId")
    Boolean tieneEnfrentamientos(@Param("categoriaId") Integer categoriaId);
    
    // ⭐ ACTUALIZADO - Contar categorías activas (en torneos EN CURSO con estado CERRADA)
    @Query("SELECT COUNT(c) FROM Categoria c " +
           "WHERE c.torneo.estado.idEstado = :estadoTorneoEnCurso " +
           "AND c.estado.idEstado = :estadoCategoriaCerrada")
    long countCategoriasActivas(
        @Param("estadoTorneoEnCurso") Integer estadoTorneoEnCurso,
        @Param("estadoCategoriaCerrada") Integer estadoCategoriaCerrada
    );
    
    // ⭐ NUEVO - Contar cuántos torneos usan esta categoría de peso
    long countByCategoriaPeso_IdCategoriaPeso(Integer idCategoriaPeso);
}