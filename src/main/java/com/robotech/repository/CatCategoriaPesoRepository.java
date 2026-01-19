package com.robotech.repository;

import com.robotech.model.CatCategoriaPeso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CatCategoriaPesoRepository extends JpaRepository<CatCategoriaPeso, Integer> {
    
    /**
     * Obtener solo categorías activas
     */
    List<CatCategoriaPeso> findByActivaTrue();
    
    /**
     * Verificar si existe una categoría con el mismo nombre
     */
    boolean existsByNombre(String nombre);
}