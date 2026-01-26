package com.robotech.service;

import com.robotech.dto.CategoriaRequest;
import com.robotech.dto.CategoriaResponse;
import com.robotech.dto.CategoriaPesoResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.CatCategoriaPeso;
import com.robotech.model.CatEstado;
import com.robotech.model.Categoria;
import com.robotech.model.Torneo;
import com.robotech.repository.CatCategoriaPesoRepository;
import com.robotech.repository.CatEstadoRepository;
import com.robotech.repository.CategoriaRepository;
import com.robotech.repository.InscripcionRepository;
import com.robotech.repository.TorneoRepository;
import com.robotech.util.Constants;
import com.robotech.util.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    @Autowired
    private CatEstadoRepository catEstadoRepository;
    
    @Autowired
    private CatCategoriaPesoRepository catCategoriaPesoRepository;
    
    @Autowired
    private InscripcionRepository inscripcionRepository;
    
    /**
     * Crear categoría (Organizador)
     */
    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
    	
        Torneo torneo = torneoRepository.findById(request.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo", "id", request.getIdTorneo()));
        
        if (!torneo.getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
            throw new BusinessException("No se pueden agregar categorías a un torneo que ya inició o finalizó");
        }
        
        ValidationUtils.validarCupoMinimoPar(request.getCupoMinimo());
        
        if (request.getCupoMaximo() < request.getCupoMinimo()) {
            throw new BusinessException("El cupo máximo no puede ser menor que el cupo mínimo");
        }
        
        CatCategoriaPeso categoriaPeso = null;
        if (request.getIdCategoriaPeso() != null) {
            categoriaPeso = catCategoriaPesoRepository.findById(request.getIdCategoriaPeso())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría de peso", "id", request.getIdCategoriaPeso()));
        }
        
        CatEstado estadoActiva = catEstadoRepository.findById(Constants.ESTADO_ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado Activa no encontrado"));
        
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setReglasEspecificas(request.getReglasEspecificas());
        categoria.setCupoMinimo(request.getCupoMinimo());
        categoria.setCupoMaximo(request.getCupoMaximo());
        categoria.setTorneo(torneo);
        categoria.setEstado(estadoActiva);
        categoria.setCategoriaPeso(categoriaPeso);
        categoria.setTieneEnfrentamientos(false);
        
        categoria = categoriaRepository.save(categoria);
        
        return convertirCategoriaAResponse(categoria);
    }
    
    /**
     * Editar categoría (Organizador)
     */
    @Transactional
    public Categoria editarCategoria(Integer categoriaId, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
        
        if (categoria.getTieneEnfrentamientos()) {
            throw new BusinessException(
                "No se puede editar la categoría porque ya tiene enfrentamientos generados. " +
                "Solo se pueden editar categorías antes de iniciar el torneo."
            );
        }
        
        if (!categoria.getTorneo().getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
            throw new BusinessException("No se pueden editar categorías de un torneo que ya inició o finalizó");
        }
        
        ValidationUtils.validarCupoMinimoPar(request.getCupoMinimo());
        
        if (request.getCupoMaximo() < request.getCupoMinimo()) {
            throw new BusinessException("El cupo máximo no puede ser menor que el cupo mínimo");
        }
        
        categoria.setNombreCategoria(request.getNombreCategoria());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setReglasEspecificas(request.getReglasEspecificas());
        categoria.setCupoMinimo(request.getCupoMinimo());
        categoria.setCupoMaximo(request.getCupoMaximo());
        categoria.setFechaModificacion(LocalDateTime.now());
        
        return categoriaRepository.save(categoria);
    }
    
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerCategoriasPublicasPorTorneo(Integer torneoId) {
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(torneoId);
        return categorias.stream()
            .map(this::convertirCategoriaAResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener categorías de un torneo
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerCategoriasPorTorneo(Integer idTorneo) {
        List<Categoria> categorias = categoriaRepository.findByTorneo_IdTorneo(idTorneo);
        
        return categorias.stream().map(cat -> {
        	
            long inscritos = inscripcionRepository.countByCategoria_IdCategoria(cat.getIdCategoria());
            
            CategoriaResponse response = new CategoriaResponse();
            response.setIdCategoria(cat.getIdCategoria());
            response.setNombreCategoria(cat.getNombreCategoria());
            response.setDescripcion(cat.getDescripcion());
            response.setReglasEspecificas(cat.getReglasEspecificas());
            response.setCupoMinimo(cat.getCupoMinimo());
            response.setCupoMaximo(cat.getCupoMaximo());
            response.setInscritosActuales((int) inscritos);
            response.setTieneEnfrentamientos(cat.getTieneEnfrentamientos());
            
            if (cat.getCategoriaPeso() != null) {
                response.setNombreCategoriaPeso(cat.getCategoriaPeso().getNombre());
            }
            
            if (cat.getTorneo() != null) {
                response.setNombreTorneo(cat.getTorneo().getNombreTorneo());
            }
            
            if (cat.getEstado() != null) {
                response.setIdEstado(cat.getEstado().getIdEstado());
                response.setEstado(cat.getEstado().getDescripcion());
            }
            
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * Obtener categorías activas de un torneo
     */
    public List<Categoria> obtenerCategoriasActivasPorTorneo(Integer torneoId) {
        return categoriaRepository.findCategoriasActivasByTorneoId(torneoId, Constants.ESTADO_ACTIVA);
    }
    
    /**
     * Obtener categoría por ID
     */
    public Categoria obtenerCategoriaPorId(Integer categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
    }
    
    /**
     * Eliminar categoría (Organizador)
     */
    @Transactional
    public void eliminarCategoria(Integer categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
        
        if (categoria.getTieneEnfrentamientos()) {
            throw new BusinessException(
                "No se puede eliminar la categoría porque ya tiene enfrentamientos generados"
            );
        }
        
        if (!categoria.getTorneo().getEstado().getIdEstado().equals(Constants.ESTADO_CONFIGURACION)) {
            throw new BusinessException("No se pueden eliminar categorías de un torneo que ya inició");
        }
        
        categoriaRepository.delete(categoria);
    }
    
    /**
     * Cambiar estado de categoría (Organizador)
     */
    @Transactional
    public Categoria cambiarEstadoCategoria(Integer categoriaId, Integer nuevoEstadoId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", categoriaId));
        
        CatEstado nuevoEstado = catEstadoRepository.findById(nuevoEstadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", "id", nuevoEstadoId));
        
        if (categoria.getTieneEnfrentamientos() && nuevoEstadoId.equals(Constants.ESTADO_ACTIVA)) {
            throw new BusinessException("No se puede activar una categoría que ya tiene enfrentamientos");
        }
        
        categoria.setEstado(nuevoEstado);
        categoria.setFechaModificacion(LocalDateTime.now());
        
        return categoriaRepository.save(categoria);
    }
    
    /**
     * Listar todas las categorías de peso activas
     */
    public List<CategoriaPesoResponse> listarCategoriasActivas() {
        return catCategoriaPesoRepository.findByActivaTrue()
            .stream()
            .map(this::convertirCategoriaPesoAResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convertir Categoria a DTO
     */
    private CategoriaResponse convertirCategoriaAResponse(Categoria categoria) {
        return new CategoriaResponse(
            categoria.getIdCategoria(),
            categoria.getNombreCategoria(),
            categoria.getDescripcion(),
            categoria.getReglasEspecificas(),
            categoria.getCupoMinimo(),
            categoria.getCupoMaximo(),
            categoria.getCategoriaPeso() != null ? 
                    categoria.getCategoriaPeso().getNombre() : null,
            categoria.getTorneo().getNombreTorneo(),
            categoria.getEstado().getIdEstado(),
            categoria.getEstado().getDescripcion(),
            categoria.getTieneEnfrentamientos(),
            0
        );
    }
    
    /**
     * Convertir CatCategoriaPeso a DTO
     */
    private CategoriaPesoResponse convertirCategoriaPesoAResponse(CatCategoriaPeso categoria) {
        return new CategoriaPesoResponse(
            categoria.getIdCategoriaPeso(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            categoria.getPesoMinimo(),
            categoria.getPesoMaximo(),
            categoria.getDimensionMaxima(),
            categoria.getActiva()
        );
    }
}