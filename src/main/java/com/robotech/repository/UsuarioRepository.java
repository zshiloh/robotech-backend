package com.robotech.repository;

import com.robotech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
    List<Usuario> findByRol_IdRol(Integer idRol);
    List<Usuario> findByActivoTrue();
}