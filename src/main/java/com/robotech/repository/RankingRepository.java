package com.robotech.repository;

import com.robotech.model.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Integer> {
    
    @Query("SELECT r FROM Ranking r " +
           "WHERE r.categoria.idCategoria = :categoriaId " +
           "ORDER BY r.posicion ASC")
    List<Ranking> findByCategoria(@Param("categoriaId") Integer categoriaId);
    
    Optional<Ranking> findByCategoria_IdCategoriaAndInscripcion_IdInscripcion(Integer categoriaId, Integer inscripcionId
    );
    
    @Modifying
    @Query("DELETE FROM Ranking r WHERE r.categoria.idCategoria = :categoriaId")
    void deleteByCategoria(@Param("categoriaId") Integer categoriaId);
    
    List<Ranking> findByCategoria_IdCategoriaOrderByPosicionAsc(Integer categoriaId);
}