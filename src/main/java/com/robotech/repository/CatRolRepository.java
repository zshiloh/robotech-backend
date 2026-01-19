package com.robotech.repository;

import com.robotech.model.CatRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatRolRepository extends JpaRepository<CatRol, Integer> {
    
    Optional<CatRol> findByNombreRol(String nombreRol);
}