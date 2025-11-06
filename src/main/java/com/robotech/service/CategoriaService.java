package com.robotech.service;

import com.robotech.model.Categoria;
import com.robotech.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * CU07: Crear categoría
     */
    public Categoria crearCategoria(Categoria categoria) {
        // Validar que el nombre no exista
        if (categoriaRepository.existsByNombreCategoria(categoria.getNombreCategoria())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        
        return categoriaRepository.save(categoria);
    }

    /**
     * CU07: Actualizar categoría
     */
    public Categoria actualizarCategoria(Integer idCategoria, Categoria categoriaActualizada) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        // Validar nombre único (si cambió)
        if (!categoria.getNombreCategoria().equals(categoriaActualizada.getNombreCategoria())) {
            if (categoriaRepository.existsByNombreCategoria(categoriaActualizada.getNombreCategoria())) {
                throw new RuntimeException("Ya existe una categoría con ese nombre");
            }
        }
        
        categoria.setNombreCategoria(categoriaActualizada.getNombreCategoria());
        categoria.setDescripcion(categoriaActualizada.getDescripcion());
        categoria.setEdadMinima(categoriaActualizada.getEdadMinima());
        categoria.setEdadMaxima(categoriaActualizada.getEdadMaxima());
        categoria.setActivo(categoriaActualizada.getActivo());
        
        return categoriaRepository.save(categoria);
    }

    /**
     * CU07: Eliminar categoría
     */
    public void eliminarCategoria(Integer idCategoria) {
        if (!categoriaRepository.existsById(idCategoria)) {
            throw new RuntimeException("Categoría no encontrada");
        }
        categoriaRepository.deleteById(idCategoria);
    }

    /**
     * Listar todas las categorías
     */
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    /**
     * Listar categorías activas
     */
    public List<Categoria> listarActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    /**
     * Buscar categoría por ID
     */
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    /**
     * Buscar categoría por nombre
     */
    public Optional<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombreCategoria(nombre);
    }

    /**
     * Activar/Desactivar categoría
     */
    public Categoria cambiarEstado(Integer idCategoria, Boolean activo) {
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        categoria.setActivo(activo);
        return categoriaRepository.save(categoria);
    }
}