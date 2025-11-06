package com.robotech.service;

import com.robotech.model.Categoria;
import com.robotech.model.Club;
import com.robotech.model.Competidor;
import com.robotech.repository.CategoriaRepository;
import com.robotech.repository.ClubRepository;
import com.robotech.repository.CompetidorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompetidorService {

    @Autowired
    private CompetidorRepository competidorRepository;
    
    @Autowired
    private ClubRepository clubRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * CU05: Inscribir Competidor
     */
    public Competidor inscribirCompetidor(Competidor competidor) {
        // Validar que el club existe y está activo
        Club club = clubRepository.findById(competidor.getClub().getIdClub())
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        if (club.getEstado() != Club.EstadoClub.Activo) {
            throw new RuntimeException("El club no está activo. Debe ser validado primero.");
        }

        // Validar que la categoría existe y está activa
        Categoria categoria = categoriaRepository.findById(competidor.getCategoria().getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        if (!categoria.getActivo()) {
            throw new RuntimeException("La categoría no está activa");
        }

        // Validar rango de edad si aplica
        if (competidor.getEdad() != null && categoria.getEdadMinima() != null && categoria.getEdadMaxima() != null) {
            if (competidor.getEdad() < categoria.getEdadMinima() || competidor.getEdad() > categoria.getEdadMaxima()) {
                throw new RuntimeException("La edad del competidor no cumple con el rango de la categoría ("
                        + categoria.getEdadMinima() + "-" + categoria.getEdadMaxima() + " años)");
            }
        }

        // Validar que el DNI no esté duplicado
        if (competidorRepository.existsByDocumentoIdentidad(competidor.getDocumentoIdentidad())) {
            throw new RuntimeException("Ya existe un competidor con el DNI " + competidor.getDocumentoIdentidad());
        }

        competidor.setClub(club);
        competidor.setCategoria(categoria);

        return competidorRepository.save(competidor);
    }

    /**
     * Listar todos los competidores
     */
    public List<Competidor> listarTodos() {
        return competidorRepository.findAll();
    }

    /**
     * Listar competidores activos
     */
    public List<Competidor> listarActivos() {
        return competidorRepository.findByActivoTrue();
    }

    /**
     * Listar competidores por club
     */
    public List<Competidor> listarPorClub(Integer idClub) {
        return competidorRepository.findByClub_IdClub(idClub);
    }

    /**
     * Listar competidores por categoría
     */
    public List<Competidor> listarPorCategoria(Integer idCategoria) {
        return competidorRepository.findByCategoria_IdCategoria(idCategoria);
    }

    /**
     * Buscar competidor por ID
     */
    public Optional<Competidor> buscarPorId(Integer id) {
        return competidorRepository.findById(id);
    }

    /**
     * Actualizar información del competidor
     */
    public Competidor actualizarCompetidor(Integer idCompetidor, Competidor competidorActualizado) {
        Competidor competidor = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));
        
        competidor.setNombre(competidorActualizado.getNombre());
        competidor.setApellido(competidorActualizado.getApellido());
        competidor.setDocumentoIdentidad(competidorActualizado.getDocumentoIdentidad());
        competidor.setFechaNacimiento(competidorActualizado.getFechaNacimiento());
        competidor.setEdad(competidorActualizado.getEdad());
        competidor.setCorreo(competidorActualizado.getCorreo());
        competidor.setTelefono(competidorActualizado.getTelefono());
        competidor.setNombreRobot(competidorActualizado.getNombreRobot());
        
        return competidorRepository.save(competidor);
    }

    /**
     * Activar/Desactivar competidor
     */
    public Competidor cambiarEstado(Integer idCompetidor, Boolean activo) {
        Competidor competidor = competidorRepository.findById(idCompetidor)
                .orElseThrow(() -> new RuntimeException("Competidor no encontrado"));
        
        competidor.setActivo(activo);
        return competidorRepository.save(competidor);
    }

    /**
     * Eliminar competidor
     */
    public void eliminarCompetidor(Integer idCompetidor) {
        if (!competidorRepository.existsById(idCompetidor)) {
            throw new RuntimeException("Competidor no encontrado");
        }
        competidorRepository.deleteById(idCompetidor);
    }
}