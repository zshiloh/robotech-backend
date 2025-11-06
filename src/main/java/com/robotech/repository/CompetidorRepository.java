package com.robotech.repository;

import com.robotech.model.Competidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompetidorRepository extends JpaRepository<Competidor, Integer> {
    List<Competidor> findByClub_IdClub(Integer idClub);
    List<Competidor> findByCategoria_IdCategoria(Integer idCategoria);
    List<Competidor> findByActivoTrue();
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
}