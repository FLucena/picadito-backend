package com.techlab.picadito.config;

import com.techlab.picadito.model.Categoria;
import com.techlab.picadito.model.Usuario;
import com.techlab.picadito.categoria.CategoriaRepository;
import com.techlab.picadito.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.demo.admin.password:AdminDemo2024!}")
    private String adminPassword;
    
    @Value("${app.demo.cliente.password:ClienteDemo2024!}")
    private String clientePassword;
    
    @Override
    public void run(String... args) {
        inicializarOActualizarUsuarios();
        if (categoriaRepository.count() == 0) {
            inicializarCategorias();
        }
    }
    
    private void inicializarOActualizarUsuarios() {
        // Crear o actualizar usuarios de ejemplo para desarrollo
        // NOTA: Estas contraseñas solo se usan en perfil 'dev'
        // En producción, este componente está deshabilitado
        
        // Admin
        Usuario admin = usuarioRepository.findByEmail("admin@picadito.com")
                .orElse(new Usuario());
        
        if (admin.getId() == null) {
            // Usuario no existe, crear nuevo
            admin.setNombre("Admin");
            admin.setEmail("admin@picadito.com");
            admin.setRol(Usuario.RolUsuario.ADMIN);
            admin.setActivo(true);
        }
        // Actualizar contraseña siempre (por si cambió en las propiedades)
        admin.setPassword(passwordEncoder.encode(adminPassword));
        usuarioRepository.save(admin);
        
        // Cliente
        Usuario cliente = usuarioRepository.findByEmail("cliente@picadito.com")
                .orElse(new Usuario());
        
        if (cliente.getId() == null) {
            // Usuario no existe, crear nuevo
            cliente.setNombre("Cliente Demo");
            cliente.setEmail("cliente@picadito.com");
            cliente.setRol(Usuario.RolUsuario.CLIENTE);
            cliente.setActivo(true);
        }
        // Actualizar contraseña siempre (por si cambió en las propiedades)
        cliente.setPassword(passwordEncoder.encode(clientePassword));
        usuarioRepository.save(cliente);
    }

    private void inicializarCategorias() {
        // Crear categorías de ejemplo
        Categoria futbol11 = new Categoria();
        futbol11.setNombre("Fútbol 11");
        futbol11.setDescripcion("Partidos de fútbol 11 vs 11");
        futbol11.setIcono("⚽");
        futbol11.setColor("#1E88E5");
        categoriaRepository.save(futbol11);

        Categoria futbol7 = new Categoria();
        futbol7.setNombre("Fútbol 7");
        futbol7.setDescripcion("Partidos de fútbol 7 vs 7");
        futbol7.setIcono("⚽");
        futbol7.setColor("#43A047");
        categoriaRepository.save(futbol7);

        Categoria futbol5 = new Categoria();
        futbol5.setNombre("Fútbol 5");
        futbol5.setDescripcion("Partidos de fútbol 5 vs 5");
        futbol5.setIcono("⚽");
        futbol5.setColor("#FB8C00");
        categoriaRepository.save(futbol5);

        Categoria mixto = new Categoria();
        mixto.setNombre("Mixto");
        mixto.setDescripcion("Partidos mixtos (hombres y mujeres)");
        mixto.setIcono("👥");
        mixto.setColor("#E91E63");
        categoriaRepository.save(mixto);

        Categoria soloHombres = new Categoria();
        soloHombres.setNombre("Solo Hombres");
        soloHombres.setDescripcion("Partidos exclusivos para hombres");
        soloHombres.setIcono("👨");
        soloHombres.setColor("#1976D2");
        categoriaRepository.save(soloHombres);

        Categoria soloMujeres = new Categoria();
        soloMujeres.setNombre("Solo Mujeres");
        soloMujeres.setDescripcion("Partidos exclusivos para mujeres");
        soloMujeres.setIcono("👩");
        soloMujeres.setColor("#C2185B");
        categoriaRepository.save(soloMujeres);

        Categoria veteranos = new Categoria();
        veteranos.setNombre("Veteranos");
        veteranos.setDescripcion("Partidos para jugadores veteranos (+35 años)");
        veteranos.setIcono("🎖️");
        veteranos.setColor("#5D4037");
        categoriaRepository.save(veteranos);

        Categoria juveniles = new Categoria();
        juveniles.setNombre("Juveniles");
        juveniles.setDescripcion("Partidos para jugadores jóvenes (-25 años)");
        juveniles.setIcono("🌟");
        juveniles.setColor("#FBC02D");
        categoriaRepository.save(juveniles);
    }
}

