package com.robotech.repository;
import com.robotech.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Integer> {
    
    @Query("SELECT r FROM Robot r JOIN FETCH r.club WHERE r.usuario.idUsuario = :idUsuario")
    Optional<Robot> findByUsuario_IdUsuario(@Param("idUsuario") Integer idUsuario);
    
    boolean existsByNombreRobot(String nombreRobot);
    
    @Query("SELECT r FROM Robot r " +
           "LEFT JOIN FETCH r.usuario " +
           "WHERE r.club.idClub = :clubId " +
           "AND r.estado = 'Activo'")
    List<Robot> findRobotsActivosByClubId(@Param("clubId") Integer clubId);
    
    @Query("SELECT COUNT(r) FROM Robot r " +
           "WHERE r.club.idClub = :clubId " +
           "AND r.estado = 'Activo'")
    int countRobotsActivosByClubId(@Param("clubId") Integer clubId);
    
    Optional<Robot> findByNombreRobot(String nombreRobot);
    
    boolean existsByUsuario_IdUsuario(Integer idUsuario);
}