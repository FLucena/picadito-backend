package com.techlab.picadito.config;

import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    
    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            inicializarDatos();
        }
    }
    
    private void inicializarDatos() {
        // Crear usuarios de ejemplo
        Usuario admin = new Usuario();
        admin.setNombre("Admin");
        admin.setEmail("admin@picadito.com");
        admin.setRol(Usuario.RolUsuario.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        
        Usuario cliente = new Usuario();
        cliente.setNombre("Cliente Demo");
        cliente.setEmail("cliente@picadito.com");
        cliente.setRol(Usuario.RolUsuario.CLIENTE);
        cliente.setActivo(true);
        usuarioRepository.save(cliente);
    }
}

