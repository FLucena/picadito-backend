package com.techlab.picadito.dto;

import com.techlab.picadito.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    
    private Long id;
    
    private String nombre;
    
    private String email;
    
    private Usuario.RolUsuario rol;
    
    private Boolean activo;
}

