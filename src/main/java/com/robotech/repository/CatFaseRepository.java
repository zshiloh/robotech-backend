package com.robotech.repository;

import com.robotech.model.CatFase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatFaseRepository extends JpaRepository<CatFase, Integer> {
    
    Optional<CatFase> findByNombreFase(String nombreFase);
}