package com.robotech.repository;

import com.robotech.model.PuntosClubHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuntosClubHistoricoRepository extends JpaRepository<PuntosClubHistorico, Integer> {

    @Query("SELECT pch FROM PuntosClubHistorico pch " +
           "WHERE pch.usuario.idUsuario = :usuarioId " +
           "AND pch.club.idClub = :clubId " +
           "AND pch.categoria.idCategoria = :categoriaId " +
           "AND pch.fechaFin IS NULL")
    Optional<PuntosClubHistorico> findActivoByUsuarioAndClubAndCategoria(
        @Param("usuarioId") Integer usuarioId,
        @Param("clubId") Integer clubId,
        @Param("categoriaId") Integer categoriaId
    );

    @Query("SELECT pch FROM PuntosClubHistorico pch " +
           "WHERE pch.usuario.idUsuario = :usuarioId " +
           "AND pch.fechaFin IS NULL")
    List<PuntosClubHistorico> findAllActivosByUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COALESCE(SUM(pch.puntosGanadosAqui), 0) " +
           "FROM PuntosClubHistorico pch " +
           "WHERE pch.club.idClub = :clubId " +
           "AND pch.categoria.idCategoria = :categoriaId")
    Integer calcularPuntosTotalesClub(
        @Param("clubId") Integer clubId,
        @Param("categoriaId") Integer categoriaId
    );

    @Query("SELECT pch.club.idClub as idClub, " +
           "pch.club.nombreClub as nombreClub, " +
           "SUM(pch.puntosGanadosAqui) as puntosHistoricos, " +
           "COUNT(DISTINCT CASE WHEN pch.fechaFin IS NULL THEN pch.usuario.idUsuario END) as competidoresActivos " +
           "FROM PuntosClubHistorico pch " +
           "WHERE pch.categoria.idCategoria = :categoriaId " +
           "GROUP BY pch.club.idClub, pch.club.nombreClub " +
           "ORDER BY puntosHistoricos DESC")
    List<Object[]> getRankingClubesPorCategoria(@Param("categoriaId") Integer categoriaId);

    @Query("SELECT pch FROM PuntosClubHistorico pch " +
           "JOIN FETCH pch.club " +
           "JOIN FETCH pch.categoria " +
           "WHERE pch.usuario.idUsuario = :usuarioId " +
           "AND pch.categoria.idCategoria = :categoriaId " +
           "ORDER BY pch.fechaInicio DESC")
    List<PuntosClubHistorico> getHistorialUsuarioPorCategoria(
        @Param("usuarioId") Integer usuarioId,
        @Param("categoriaId") Integer categoriaId
    );

    @Query("SELECT pch FROM PuntosClubHistorico pch " +
           "JOIN FETCH pch.usuario " +
           "JOIN FETCH pch.categoria " +
           "WHERE pch.club.idClub = :clubId " +
           "AND pch.categoria.idCategoria = :categoriaId " +
           "ORDER BY pch.fechaInicio DESC")
    List<PuntosClubHistorico> getHistorialClubPorCategoria(
        @Param("clubId") Integer clubId,
        @Param("categoriaId") Integer categoriaId
    );

    @Query("SELECT COUNT(DISTINCT pch.usuario.idUsuario) " +
           "FROM PuntosClubHistorico pch " +
           "WHERE pch.club.idClub = :clubId " +
           "AND pch.categoria.idCategoria = :categoriaId " +
           "AND pch.fechaFin IS NULL")
    Long contarCompetidoresActivosPorClub(
        @Param("clubId") Integer clubId,
        @Param("categoriaId") Integer categoriaId
    );
    
    boolean existsByUsuario_IdUsuario(Integer idUsuario);
    
    long countByUsuario_IdUsuario(Integer idUsuario);
}