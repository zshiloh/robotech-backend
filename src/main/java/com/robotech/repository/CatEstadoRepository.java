package com.robotech.repository;

import com.robotech.model.CatEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatEstadoRepository extends JpaRepository<CatEstado, Integer> {
    
    Optional<CatEstado> findByDescripcion(String descripcion);
}