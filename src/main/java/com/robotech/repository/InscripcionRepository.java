package com.robotech.repository;

import com.robotech.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    
    List<Inscripcion> findByCategoria_IdCategoria(Integer idCategoria);
    
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.categoria.idCategoria = :categoriaId")
    int countByCategoria(@Param("categoriaId") Integer categoriaId);
    
    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i " +
           "WHERE i.usuario.idUsuario = :usuarioId " +
           "AND i.categoria.idCategoria = :categoriaId")
    boolean existsByUsuarioAndCategoria(
        @Param("usuarioId") Integer usuarioId,
        @Param("categoriaId") Integer categoriaId
    );
    
    Optional<Inscripcion> findByUsuario_IdUsuarioAndCategoria_IdCategoria(
        Integer usuarioId, 
        Integer categoriaId
    );
    
    @Query("SELECT i FROM Inscripcion i " +
           "WHERE i.categoria.idCategoria = :categoriaId " +
           "ORDER BY i.puntajeAcumulado DESC")
    List<Inscripcion> findByCategoriOrderByPuntaje(@Param("categoriaId") Integer categoriaId);
    
    List<Inscripcion> findByUsuario_IdUsuario(Integer idUsuario);
    
    @Query("SELECT i FROM Inscripcion i " +
           "JOIN i.usuario u " +
           "JOIN Robot r ON r.usuario.idUsuario = u.idUsuario " +
           "WHERE r.club.idClub = :clubId " +
           "AND i.estado = 'PENDIENTE'")
    List<Inscripcion> findInscripcionesPendientesPorClub(@Param("clubId") Integer clubId);
    
    boolean existsByUsuario_IdUsuarioAndCategoria_IdCategoria(Integer idUsuario, Integer idCategoria);
    
    long countByCategoria_IdCategoria(Integer idCategoria);
}