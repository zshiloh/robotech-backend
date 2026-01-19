package com.robotech.service;

import com.robotech.dto.SedeRequest;
import com.robotech.dto.SedeResponse;
import com.robotech.exception.BusinessException;
import com.robotech.exception.ResourceNotFoundException;
import com.robotech.model.Sede;
import com.robotech.repository.SedeRepository;
import com.robotech.repository.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SedeService {
    
    @Autowired
    private SedeRepository sedeRepository;
    
    @Autowired
    private TorneoRepository torneoRepository;
    
    /**
     * Crear una nueva sede (Admin)
     */
    @Transactional
    public SedeResponse crearSede(SedeRequest request) {
    	
        if (sedeRepository.existsByNombreSede(request.getNombreSede())) {
            throw new BusinessException("Ya existe una sede con el nombre '" + request.getNombreSede() + "'");
        }
        
        Sede sede = new Sede();
        sede.setNombreSede(request.getNombreSede().trim());
        sede.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        
        sede = sedeRepository.save(sede);
        
        return convertirAResponse(sede);
    }
    
    /**
     * Actualizar una sede existente (Admin)
     */
    @Transactional
    public SedeResponse actualizarSede(Integer idSede, SedeRequest request) {
        
        Sede sede = sedeRepository.findById(idSede)
                .orElseThrow(() -> new ResourceNotFoundException("Sede", "id", idSede));
        
        if (!sede.getNombreSede().equals(request.getNombreSede()) && 
            sedeRepository.existsByNombreSede(request.getNombreSede())) {
            throw new BusinessException("Ya existe otra sede con el nombre '" + request.getNombreSede() + "'");
        }
        
        sede.setNombreSede(request.getNombreSede().trim());
        sede.setDireccion(request.getDireccion() != null ? request.getDireccion().trim() : null);
        
        sede = sedeRepository.save(sede);
        
        return convertirAResponse(sede);
    }
    
    /**
     * Eliminar una sede (Admin)
     */
    @Transactional
    public void eliminarSede(Integer idSede) {
        
        Sede sede = sedeRepository.findById(idSede)
                .orElseThrow(() -> new ResourceNotFoundException("Sede", "id", idSede));
        
        if (torneoRepository.existsBySede_IdSede(idSede)) {
            throw new BusinessException("No se puede eliminar la sede porque tiene torneos asociados");
        }
        
        sedeRepository.delete(sede);
    }
    
    /**
     * Obtener todas las sedes (Público)
     */
    public List<SedeResponse> listarSedes() {
        List<Sede> sedes = sedeRepository.findAll();
        
        return sedes.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener una sede por ID (Público)
     */
    public SedeResponse obtenerSedePorId(Integer idSede) {
        Sede sede = sedeRepository.findById(idSede)
                .orElseThrow(() -> new ResourceNotFoundException("Sede", "id", idSede));
        
        return convertirAResponse(sede);
    }
    
    /**
     * Convertir entidad a DTO
     */
    private SedeResponse convertirAResponse(Sede sede) {
        return new SedeResponse(
            sede.getIdSede(),
            sede.getNombreSede(),
            sede.getDireccion()
        );
    }
}