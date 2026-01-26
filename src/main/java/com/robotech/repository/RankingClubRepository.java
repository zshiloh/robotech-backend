package com.robotech.repository;

import com.robotech.model.RankingClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingClubRepository extends JpaRepository<RankingClub, Integer> {
    
    @Query("SELECT rc FROM RankingClub rc " +
           "WHERE rc.categoria.idCategoria = :categoriaId " +
           "ORDER BY rc.posicionClub ASC")
    List<RankingClub> findByCategoria(@Param("categoriaId") Integer categoriaId);
    
    Optional<RankingClub> findByCategoria_IdCategoriaAndClub_IdClub(Integer categoriaId, Integer clubId
    );
    
    @Modifying
    @Query("DELETE FROM RankingClub rc WHERE rc.categoria.idCategoria = :categoriaId")
    void deleteByCategoria(@Param("categoriaId") Integer categoriaId);
}