package com.example.config;

import com.example.model.Rol;
import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * ⚠️ DEPRECADO: Esta clase ya no se usa.
 * El usuario ADMIN se crea desde EjemplosDataSeeder.java
 * 
 * Esta clase está desactivada porque EjemplosDataSeeder ya crea
 * un administrador de ejemplo.
 * 
 * Si necesitas un admin adicional con credenciales diferentes,
 * reactiva esta clase cambiando @Profile("never") a @Profile("prod")
 * y configura las credenciales seguras.
 */
@Component
@Profile("never")  // ← DESACTIVADO: EjemplosDataSeeder ya crea el admin
public class AdminInitializer implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // ESTA CLASE YA NO SE EJECUTA
        // El admin se crea desde EjemplosDataSeeder.java
        System.out.println("⚠️ AdminInitializer DEPRECADO: EjemplosDataSeeder ya crea el admin");
        
        // Email del administrador (CAMBIAR ANTES DE DESPLEGAR)
        String adminEmail = "admin@pixelshop.com";
        
        // Solo crear si no existe ningún usuario ADMIN
        if (!existeAdmin()) {
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║  ⚠️  CLASE DESACTIVADA - NO SE EJECUTA          ║");
            System.out.println("║  📝 Admin creado por: EjemplosDataSeeder.java    ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            
            // ⚠️ CAMBIAR ESTA CONTRASEÑA POR UNA SEGURA
            // Ejemplo: Usa un generador de contraseñas
            // https://www.lastpass.com/es/features/password-generator
            admin.setPassword(passwordEncoder.encode("CAMBIAR_ESTA_CONTRASEÑA_AHORA"));
            
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);
            
            usuarioRepository.save(admin);
            
            System.out.println("✅ Usuario ADMINISTRADOR creado exitosamente");
            System.out.println("⚠️  RECUERDA: Cambiar la contraseña después del primer login");
        } else {
            System.out.println("ℹ️ AdminInitializer: Ya existe un usuario ADMIN");
        }
    }
    
    /**
     * Verifica si ya existe al menos un usuario con rol ADMIN.
     */
    private boolean existeAdmin() {
        return usuarioRepository.findAll().stream()
            .anyMatch(usuario -> usuario.getRol() == Rol.ADMIN);
    }
}
