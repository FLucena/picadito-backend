package com.techlab.picadito.dto;

import java.util.List;

/**
 * DTO wrapper para listas de categorías.
 * Facilita agregar paginación, metadatos y otros campos en el futuro.
 */
public class CategoriasResponseDTO {
    
    private List<CategoriaResponseDTO> categorias;
    private int total;
    
    public CategoriasResponseDTO() {
    }
    
    public CategoriasResponseDTO(List<CategoriaResponseDTO> categorias) {
        this.categorias = categorias;
        this.total = categorias != null ? categorias.size() : 0;
    }
    
    public List<CategoriaResponseDTO> getCategorias() {
        return categorias;
    }
    
    public void setCategorias(List<CategoriaResponseDTO> categorias) {
        this.categorias = categorias;
        this.total = categorias != null ? categorias.size() : 0;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

