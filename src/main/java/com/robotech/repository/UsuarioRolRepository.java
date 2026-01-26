package com.robotech.repository;

import com.robotech.model.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRol.UsuarioRolId> {
    
    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.id.idUsuario = :usuarioId AND ur.id.idRol = :rolId")
    void deleteByUsuarioIdAndRolId(@Param("usuarioId") Integer usuarioId, @Param("rolId") Integer rolId);
    
    @Query("SELECT COUNT(ur) > 0 FROM UsuarioRol ur WHERE ur.id.idUsuario = :usuarioId AND ur.id.idRol = :rolId")
    boolean existsByUsuarioIdAndRolId(@Param("usuarioId") Integer usuarioId, @Param("rolId") Integer rolId);
}