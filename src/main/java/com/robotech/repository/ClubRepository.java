package com.robotech.repository;

import com.robotech.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Integer> {
    List<Club> findByEstado(Club.EstadoClub estado);
    boolean existsByNombreClub(String nombreClub);
    boolean existsByCorreoRepresentante(String correoRepresentante);
}