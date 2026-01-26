package com.robotech.service;

import com.robotech.dto.CrearRobotRequest;
import com.robotech.dto.ActualizarRobotRequest;
import com.robotech.dto.RobotResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.CatCategoriaPeso;
import com.robotech.model.Club;
import com.robotech.model.Robot;
import com.robotech.model.Usuario;
import com.robotech.repository.CatCategoriaPesoRepository;
import com.robotech.repository.ClubRepository;
import com.robotech.repository.RobotRepository;
import com.robotech.repository.TorneoRepository;
import com.robotech.repository.UsuarioRepository;
import com.robotech.util.Constants;
import com.robotech.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RobotService {
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private CatCategoriaPesoRepository catCategoriaPesoRepository;
    
    /**
     * Crear robot automáticamente (interno del sistema)
     */
    @Transactional
    public Robot crearRobotAutomatico(Integer idUsuario, Integer idClub) {
    	
        Optional<Robot> robotExistente = robotRepository.findByUsuario_IdUsuario(idUsuario);
        
        if (robotExistente.isPresent()) {
        	
            Robot robot = robotExistente.get();
            
            Club club = clubRepository.findById(idClub)
                    .orElseThrow(() -> new ResourceNotFoundException("Club", "id", idClub));
            
            robot.setClub(club);
            robot.setEstado(Robot.EstadoRobot.ACTIVO);
            robot.setRazonInactividad(null);
            
            return robotRepository.save(robot);
        }
        
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", idClub));
        
        String nombreRobot = "Robot de " + usuario.getNombreCompleto();
        int contador = 1;
        
        while (robotRepository.existsByNombreRobot(nombreRobot)) {
            nombreRobot = "Robot de " + usuario.getNombreCompleto() + " " + contador;
            contador++;
        }
        
        Robot robot = new Robot();
        robot.setNombreRobot(nombreRobot);
        robot.setUsuario(usuario);
        robot.setClub(club);
        robot.setEstado(Robot.EstadoRobot.ACTIVO);
        robot.setEstadoActividad(Robot.EstadoActividadRobot.ACTIVO);
        
        return robotRepository.save(robot);
    }
    
    /**
     * Obtener perfil del competidor (su robot)
     */
    public RobotResponse obtenerMiPerfil(Integer idUsuario) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un robot creado"));
        
        return convertirAResponse(robot);
    }
    
    /**
     * Completar especificaciones técnicas del robot (peso, dimensiones, categoría)
     */
    @Transactional
    public RobotResponse completarEspecificaciones(Integer idUsuario, CrearRobotRequest request) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes un robot creado"));
        
        if (torneoRepository.existeTorneoEnCurso(Constants.ESTADO_EN_CURSO)) {
            throw new BusinessException("No puedes modificar las especificaciones mientras hay un torneo en curso");
        }
        
        CatCategoriaPeso categoria = catCategoriaPesoRepository.findById(request.getIdCategoriaPeso())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", request.getIdCategoriaPeso()));
        
        if (request.getPeso() < categoria.getPesoMinimo() || request.getPeso() > categoria.getPesoMaximo()) {
            throw new BusinessException(
                String.format("El peso %d gramos no cumple con la categoría %s (%d-%d gramos)",
                    request.getPeso(), categoria.getNombre(), 
                    categoria.getPesoMinimo(), categoria.getPesoMaximo())
            );
        }
        
        validarDimensiones(request.getDimensiones(), categoria.getDimensionMaxima());
        
        ValidationUtils.validarNombreRobot(request.getNombreRobot());
        if (!request.getNombreRobot().equals(robot.getNombreRobot()) && 
            robotRepository.existsByNombreRobot(request.getNombreRobot())) {
            throw new BusinessException("El nombre del robot '" + request.getNombreRobot() + "' ya está en uso");
        }
        
        robot.setNombreRobot(request.getNombreRobot());
        robot.setDescripcion(request.getDescripcion());
        robot.setPeso(request.getPeso());
        robot.setDimensiones(request.getDimensiones());
        robot.setCategoriaPeso(categoria);
        
        if (robot.getVictorias() == null) robot.setVictorias(0);
        if (robot.getDerrotas() == null) robot.setDerrotas(0);
        if (robot.getPuntosTotales() == null) robot.setPuntosTotales(0);
        
        robot = robotRepository.save(robot);
        
        return convertirAResponse(robot);
    }
    
    /**
     * Actualizar especificaciones del robot existente
     */
    @Transactional
    public RobotResponse actualizarEspecificaciones(Integer idUsuario, ActualizarRobotRequest request) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Robot", "idUsuario", idUsuario));
        
        if (torneoRepository.existeTorneoEnCurso(Constants.ESTADO_EN_CURSO)) {
            throw new BusinessException("No puedes modificar las especificaciones mientras hay un torneo en curso");
        }
        
        if (request.getNombreRobot() != null) {
            ValidationUtils.validarNombreRobot(request.getNombreRobot());
            if (!request.getNombreRobot().equals(robot.getNombreRobot()) && 
                robotRepository.existsByNombreRobot(request.getNombreRobot())) {
                throw new BusinessException("El nombre del robot '" + request.getNombreRobot() + "' ya está en uso");
            }
            robot.setNombreRobot(request.getNombreRobot());
        }
        
        if (request.getDescripcion() != null) {
            robot.setDescripcion(request.getDescripcion());
        }
        
        CatCategoriaPeso categoria = robot.getCategoriaPeso();
        if (request.getIdCategoriaPeso() != null) {
            categoria = catCategoriaPesoRepository.findById(request.getIdCategoriaPeso())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", request.getIdCategoriaPeso()));
            robot.setCategoriaPeso(categoria);
        }
        
        if (request.getPeso() != null) {
            if (categoria == null) {
                throw new BusinessException("Debes especificar una categoría de peso para validar el peso");
            }
            if (request.getPeso() < categoria.getPesoMinimo() || request.getPeso() > categoria.getPesoMaximo()) {
                throw new BusinessException(
                    String.format("El peso %d gramos no cumple con la categoría %s (%d-%d gramos)",
                        request.getPeso(), categoria.getNombre(), 
                        categoria.getPesoMinimo(), categoria.getPesoMaximo())
                );
            }
            robot.setPeso(request.getPeso());
        }
        
        if (request.getDimensiones() != null) {
            if (categoria == null) {
                throw new BusinessException("Debes especificar una categoría de peso para validar las dimensiones");
            }
            validarDimensiones(request.getDimensiones(), categoria.getDimensionMaxima());
            robot.setDimensiones(request.getDimensiones());
        }
        
        robot = robotRepository.save(robot);
        
        return convertirAResponse(robot);
    }
    
    /**
     * Obtener robot con especificaciones completas
     */
    @Transactional(readOnly = true)
    public RobotResponse obtenerRobotCompleto(Integer idUsuario) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Robot", "idUsuario", idUsuario));
        
        return convertirAResponse(robot);
    }
    
    /**
     * Marcar robot como inactivo (cuando competidor sale del club)
     */
    @Transactional
    public void marcarRobotInactivo(Integer idUsuario, String razon) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Robot no encontrado"));
        
        robot.setEstado(Robot.EstadoRobot.INACTIVO);
        robot.setClub(null);
        robot.setRazonInactividad(razon);
        
        robotRepository.save(robot);
    }
    
    /**
     * Reactivar robot (cuando competidor se une a nuevo club)
     */
    @Transactional
    public void reactivarRobot(Integer idUsuario, Integer idClub) {
        Robot robot = robotRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Robot no encontrado"));
        
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new ResourceNotFoundException("Club", "id", idClub));
        
        robot.setEstado(Robot.EstadoRobot.ACTIVO);
        robot.setClub(club);
        robot.setRazonInactividad(null);
        
        robotRepository.save(robot);
    }
    
    /**
     * Obtener robot por ID de usuario
     */
    public Optional<Robot> obtenerRobotPorUsuario(Integer idUsuario) {
        return robotRepository.findByUsuario_IdUsuario(idUsuario);
    }
    
    /**
     * Validar dimensiones
     */
    private void validarDimensiones(String dimensiones, Integer limiteMaximo) {
        String[] partes = dimensiones.split("x");
        
        if (partes.length != 3) {
            throw new BusinessException("Las dimensiones deben tener 3 valores separados por 'x' (LxWxH)");
        }
        
        for (String parte : partes) {
            try {
                int dimension = Integer.parseInt(parte.trim());
                if (dimension <= 0) {
                    throw new BusinessException("Las dimensiones deben ser valores positivos");
                }
                if (dimension > limiteMaximo) {
                    throw new BusinessException(
                        String.format("Ninguna dimensión puede exceder %d cm", limiteMaximo)
                    );
                }
            } catch (NumberFormatException e) {
                throw new BusinessException("Las dimensiones deben ser números enteros");
            }
        }
    }
    
    /**
     * Convertir entidad a DTO
     */
    private RobotResponse convertirAResponse(Robot robot) {
        return new RobotResponse(
            robot.getIdRobot(),
            robot.getNombreRobot(),
            robot.getDescripcion(),
            robot.getPeso(),
            robot.getDimensiones(),
            robot.getCategoriaPeso() != null ? robot.getCategoriaPeso().getNombre() : null,
            robot.getVictorias() != null ? robot.getVictorias() : 0,
            robot.getDerrotas() != null ? robot.getDerrotas() : 0,
            robot.getPuntosTotales() != null ? robot.getPuntosTotales() : 0,
            robot.getClub() != null ? robot.getClub().getNombreClub() : null,
            robot.getClub() != null ? robot.getClub().getIdClub() : null,
            robot.getEstado().name(),
            robot.getRazonInactividad()
        );
    }
}