package com.robotech.service;

import com.robotech.model.Club;
import com.robotech.model.Usuario;
import com.robotech.repository.ClubRepository;
import com.robotech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClubService {

    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * CU01: Registrar Club
     * Registra un nuevo club con estado "Pendiente"
     */
    public Club registrarClub(Club club) {
        // Validar que el nombre no exista
        if (clubRepository.existsByNombreClub(club.getNombreClub())) {
            throw new RuntimeException("Ya existe un club con ese nombre");
        }
        
        // Validar que el correo no exista
        if (clubRepository.existsByCorreoRepresentante(club.getCorreoRepresentante())) {
            throw new RuntimeException("El correo del representante ya está registrado");
        }
        
        // Establecer estado inicial
        club.setEstado(Club.EstadoClub.Pendiente);
        
        return clubRepository.save(club);
    }

    /**
     * CU02: Validar Club (Aprobar)
     * Cambia el estado del club a "Activo"
     */
    public Club aprobarClub(Integer idClub, Integer idAdminValidador) {
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
        
        Usuario admin = usuarioRepository.findById(idAdminValidador)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        
        // Validar que el club esté pendiente
        if (club.getEstado() != Club.EstadoClub.Pendiente) {
            throw new RuntimeException("El club no está pendiente de validación");
        }
        
        club.setEstado(Club.EstadoClub.Activo);
        club.setFechaValidacion(LocalDateTime.now());
        club.setUsuarioValidador(admin);
        
        return clubRepository.save(club);
    }

    /**
     * CU02: Validar Club (Rechazar)
     * Cambia el estado del club a "Rechazado"
     */
    public Club rechazarClub(Integer idClub, Integer idAdminValidador, String observaciones) {
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
        
        Usuario admin = usuarioRepository.findById(idAdminValidador)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        
        club.setEstado(Club.EstadoClub.Rechazado);
        club.setFechaValidacion(LocalDateTime.now());
        club.setUsuarioValidador(admin);
        club.setObservaciones(observaciones);
        
        return clubRepository.save(club);
    }

    /**
     * Listar todos los clubes
     */
    public List<Club> listarTodos() {
        return clubRepository.findAll();
    }

    /**
     * Listar clubes por estado
     */
    public List<Club> listarPorEstado(Club.EstadoClub estado) {
        return clubRepository.findByEstado(estado);
    }

    /**
     * Listar clubes pendientes (para validación)
     */
    public List<Club> listarPendientes() {
        return clubRepository.findByEstado(Club.EstadoClub.Pendiente);
    }

    /**
     * Listar clubes activos
     */
    public List<Club> listarActivos() {
        return clubRepository.findByEstado(Club.EstadoClub.Activo);
    }

    /**
     * Buscar club por ID
     */
    public Optional<Club> buscarPorId(Integer id) {
        return clubRepository.findById(id);
    }

    /**
     * Actualizar información del club
     */
    public Club actualizarClub(Integer idClub, Club clubActualizado) {
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
        
        club.setNombreClub(clubActualizado.getNombreClub());
        club.setRepresentante(clubActualizado.getRepresentante());
        club.setCorreoRepresentante(clubActualizado.getCorreoRepresentante());
        club.setTelefono(clubActualizado.getTelefono());
        club.setDireccion(clubActualizado.getDireccion());
        
        return clubRepository.save(club);
    }

    /**
     * Eliminar club
     */
    public void eliminarClub(Integer idClub) {
        if (!clubRepository.existsById(idClub)) {
            throw new RuntimeException("Club no encontrado");
        }
        clubRepository.deleteById(idClub);
    }
}