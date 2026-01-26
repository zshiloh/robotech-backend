package com.robotech.service;

import com.robotech.model.Club;
import com.robotech.model.Robot;
import com.robotech.model.Usuario;
import com.robotech.repository.ClubRepository;
import com.robotech.repository.RobotRepository;
import com.robotech.repository.UsuarioRepository;
import com.robotech.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InactividadScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(InactividadScheduler.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    /**
     * Job que se ejecuta todos los días a las 2 AM
     * Detecta inactividad de competidores y representantes
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void detectarInactividad() {
        logger.info("========================================");
        logger.info("Iniciando detección de inactividad - {}", LocalDateTime.now());
        logger.info("========================================");
        
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace30Dias = ahora.minusDays(Constants.DIAS_INACTIVIDAD_COMPETIDOR);
        LocalDateTime hace7Dias = ahora.minusDays(Constants.DIAS_INACTIVIDAD_REPRESENTANTE_ALERTA);
        
        detectarCompetidoresInactivos(hace30Dias);
        
        detectarRepresentantesInactivos7Dias(hace7Dias);
        
        detectarRepresentantesInactivos30Dias(hace30Dias);
        
        logger.info("========================================");
        logger.info("Detección de inactividad finalizada - {}", LocalDateTime.now());
        logger.info("========================================");
    }
    
    /**
     * Detectar competidores inactivos (30+ días)
     */
    private void detectarCompetidoresInactivos(LocalDateTime fechaLimite) {
        logger.info("Buscando competidores inactivos (30+ días)...");
        
        List<Usuario> competidoresInactivos = usuarioRepository
                .findCompetidoresInactivosMasDe30Dias(Constants.ROL_COMPETIDOR, fechaLimite);
        
        logger.info("Encontrados {} competidores inactivos", competidoresInactivos.size());
        
        for (Usuario competidor : competidoresInactivos) {
        	
            robotRepository.findByUsuario_IdUsuario(competidor.getIdUsuario())
                    .ifPresent(robot -> {
                        if (robot.getClub() != null && robot.getEstado() == Robot.EstadoRobot.ACTIVO) {
                            Club club = robot.getClub();
                            Usuario representante = club.getRepresentante();
                            
                            logger.info("Competidor inactivo: {} (Club: {})", 
                                competidor.getNombreCompleto(), 
                                club.getNombreClub());
                            
                            notificacionService.crearNotificacion(
                                representante.getIdUsuario(),
                                Constants.NOTIF_COMPETIDOR_INACTIVO_30D,
                                "Competidor inactivo",
                                "El competidor '" + competidor.getNombreCompleto() + 
                                "' lleva más de 30 días sin iniciar sesión. " +
                                "Considera si deseas mantenerlo en el club."
                            );
                            
                            notificacionService.crearNotificacion(
                                competidor.getIdUsuario(),
                                Constants.NOTIF_COMPETIDOR_INACTIVO_30D,
                                "Advertencia de inactividad",
                                "Has estado inactivo por más de 30 días. " +
                                "El representante de tu club ha sido notificado. " +
                                "Inicia sesión pronto para mantener tu actividad."
                            );
                        }
                    });
        }
    }
    
    /**
     * Detectar representantes inactivos (7+ días)
     */
    private void detectarRepresentantesInactivos7Dias(LocalDateTime fechaLimite) {
        logger.info("Buscando representantes inactivos (7+ días)...");
        
        List<Club> clubes = clubRepository.findClubesConRepresentanteInactivo(fechaLimite);
        
        int clubesProcesados = 0;
        
        for (Club club : clubes) {
        	
            if (club.getEstadoActividad() == Club.EstadoActividad.ACTIVO) {
                
                logger.info("Representante inactivo 7+ días - Club: {} (Representante: {})",
                    club.getNombreClub(),
                    club.getRepresentante().getNombreCompleto());
                
                club.setEstadoActividad(Club.EstadoActividad.INACTIVO_7D);
                clubRepository.save(club);
                
                List<Robot> robots = robotRepository.findRobotsActivosByClubId(club.getIdClub());
                
                for (Robot robot : robots) {
                    notificacionService.crearNotificacion(
                        robot.getUsuario().getIdUsuario(),
                        Constants.NOTIF_REPRESENTANTE_INACTIVO_7D,
                        "Representante inactivo",
                        "El representante del club '" + club.getNombreClub() + 
                        "' lleva más de 7 días sin iniciar sesión. " +
                        "Si deseas salir del club, no contará como cambio de club."
                    );
                }
                
                clubesProcesados++;
            }
        }
        
        logger.info("Clubes marcados como Inactivo_7d: {}", clubesProcesados);
    }
    
    /**
     * Detectar representantes inactivos (30+ días)
     */
    private void detectarRepresentantesInactivos30Dias(LocalDateTime fechaLimite) {
        logger.info("Buscando representantes inactivos (30+ días)...");
        
        List<Club> clubes = clubRepository.findClubesConRepresentanteInactivo(fechaLimite);
        
        int clubesProcesados = 0;
        
        for (Club club : clubes) {
        	
            if (club.getEstadoActividad() != Club.EstadoActividad.INACTIVO) {
                
                logger.info("Representante inactivo 30+ días - Club: {} (Representante: {})",
                    club.getNombreClub(),
                    club.getRepresentante().getNombreCompleto());
                
                club.setEstadoActividad(Club.EstadoActividad.INACTIVO);
                club.setAceptaSolicitudes(false);
                clubRepository.save(club);
                
                List<Robot> robots = robotRepository.findRobotsActivosByClubId(club.getIdClub());
                
                for (Robot robot : robots) {
                    notificacionService.crearNotificacion(
                        robot.getUsuario().getIdUsuario(),
                        Constants.NOTIF_CLUB_MARCADO_INACTIVO,
                        "Club marcado como inactivo",
                        "El club '" + club.getNombreClub() + 
                        "' ha sido marcado como inactivo porque el representante lleva más de 30 días sin iniciar sesión. " +
                        "Puedes salir del club sin penalización o esperar a que el representante regrese."
                    );
                }
                
                notificacionService.crearNotificacion(
                    club.getRepresentante().getIdUsuario(),
                    Constants.NOTIF_CLUB_MARCADO_INACTIVO,
                    "Tu club fue marcado como inactivo",
                    "Tu club '" + club.getNombreClub() + 
                    "' fue marcado como inactivo por falta de actividad. " +
                    "Inicia sesión y reactiva tu club para seguir participando."
                );
                
                clubesProcesados++;
            }
        }
        
        logger.info("Clubes marcados como Inactivo: {}", clubesProcesados);
    }
}