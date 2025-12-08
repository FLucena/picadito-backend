package com.techlab.picadito.categoria;

import com.techlab.picadito.dto.CategoriaDTO;
import com.techlab.picadito.dto.CategoriaResponseDTO;
import com.techlab.picadito.dto.CategoriasResponseDTO;
import com.techlab.picadito.exception.BusinessException;
import com.techlab.picadito.exception.ResourceNotFoundException;
import com.techlab.picadito.model.Categoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);
    private static final String CACHE_CATEGORIAS = "categorias";

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Cacheable(value = CACHE_CATEGORIAS, key = "'all'")
    public CategoriasResponseDTO obtenerTodas() {
        logger.debug("Obteniendo todas las categorías");
        List<CategoriaResponseDTO> categorias = categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return new CategoriasResponseDTO(categorias);
    }

    @Cacheable(value = CACHE_CATEGORIAS, key = "#id")
    public CategoriaResponseDTO obtenerPorId(@NonNull Long id) {
        logger.debug("Buscando categoría con id: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return convertirADTO(categoria);
    }

    @CacheEvict(value = CACHE_CATEGORIAS, allEntries = true)
    public CategoriaResponseDTO crear(CategoriaDTO categoriaDTO) {
        logger.info("Creando nueva categoría: {}", categoriaDTO.getNombre());
        
        if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + categoriaDTO.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setDescripcion(categoriaDTO.getDescripcion());
        categoria.setIcono(categoriaDTO.getIcono());
        categoria.setColor(categoriaDTO.getColor());

        categoria = categoriaRepository.save(categoria);
        logger.info("Categoría creada exitosamente con id: {}", categoria.getId());
        return convertirADTO(categoria);
    }

    @CacheEvict(value = CACHE_CATEGORIAS, allEntries = true)
    public CategoriaResponseDTO actualizar(@NonNull Long id, CategoriaDTO categoriaDTO) {
        logger.info("Actualizando categoría con id: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        // Validar que el nombre no esté duplicado (si cambió)
        if (!categoria.getNombre().equals(categoriaDTO.getNombre()) && 
            categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new BusinessException("Ya existe una categoría con el nombre: " + categoriaDTO.getNombre());
        }

        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setDescripcion(categoriaDTO.getDescripcion());
        categoria.setIcono(categoriaDTO.getIcono());
        categoria.setColor(categoriaDTO.getColor());

        categoria = categoriaRepository.save(categoria);
        logger.info("Categoría actualizada exitosamente");
        return convertirADTO(categoria);
    }

    @CacheEvict(value = CACHE_CATEGORIAS, allEntries = true)
    public void eliminar(@NonNull Long id) {
        logger.info("Eliminando categoría con id: {}", id);
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría no encontrada con id: " + id);
        }
        categoriaRepository.deleteById(id);
        logger.info("Categoría eliminada exitosamente");
    }

    public Categoria obtenerCategoriaEntity(@NonNull Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    private CategoriaResponseDTO convertirADTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setIcono(categoria.getIcono());
        dto.setColor(categoria.getColor());
        dto.setFechaCreacion(categoria.getFechaCreacion());
        dto.setFechaActualizacion(categoria.getFechaActualizacion());
        return dto;
    }
}

