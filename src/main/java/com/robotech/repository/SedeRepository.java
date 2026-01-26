package com.robotech.repository;

import com.robotech.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Integer> {
    
    Optional<Sede> findByNombreSede(String nombreSede);
    
    boolean existsByNombreSede(String nombreSede);
}