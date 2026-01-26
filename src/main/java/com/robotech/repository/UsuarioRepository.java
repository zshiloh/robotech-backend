package com.robotech.repository;

import com.robotech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByTelefono(String telefono);
    
    boolean existsByApodoPersonal(String apodoPersonal);
    
    @Query("SELECT u FROM Usuario u " +
           "JOIN u.roles r " +
           "WHERE r.idRol = :rolCompetidor " +
           "AND u.ultimoInicioSesion < :fechaLimite")
    List<Usuario> findCompetidoresInactivosMasDe30Dias(
        @Param("rolCompetidor") Integer rolCompetidor,
        @Param("fechaLimite") LocalDateTime fechaLimite
    );
    
    @Query("SELECT u FROM Usuario u " +
           "JOIN u.roles r " +
           "WHERE r.idRol = :rolId")
    List<Usuario> findByRolId(@Param("rolId") Integer rolId);
}