package com.example.config;

import com.example.model.*;
import com.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * ⚠️ DEPRECADO: Esta clase ya no se usa.
 * Los datos de ejemplo ahora se cargan desde EjemplosDataSeeder.java
 * 
 * Esta clase está desactivada y solo se mantiene como referencia.
 * Para cargar datos de ejemplo, usar EjemplosDataSeeder que se ejecuta siempre.
 */
@Component
@Profile("never")  // ← DESACTIVADO: nunca se ejecuta
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Autowired
    private BibliotecaUsuarioRepository bibliotecaRepository;
    
    @Autowired
    private PublicacionJuegoRepository publicacionRepository;
    
    @Autowired
    private MovimientoProveedorRepository movimientoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // ESTA CLASE YA NO SE EJECUTA
        // Los datos de ejemplo se cargan desde EjemplosDataSeeder.java
        System.out.println("⚠️ DataSeeder DEPRECADO: Usar EjemplosDataSeeder en su lugar");
        
        if (usuarioRepository.count() == 0) {
            System.out.println("╔══════════════════════════════════════════════════╗");
            System.out.println("║  ⚠️  CLASE DESACTIVADA - NO SE EJECUTA          ║");
            System.out.println("║  📝 Usar: EjemplosDataSeeder.java                ║");
            System.out.println("╚══════════════════════════════════════════════════╝");
            
            // 1. CREAR USUARIOS
            crearUsuarios();
            
            // 2. CREAR JUEGOS
            crearJuegos();
            
            // 3. CREAR PUBLICACIONES
            crearPublicaciones();
            
            // 4. CREAR COMPRAS DE EJEMPLO
            crearComprasEjemplo();
            
            System.out.println("\n✅ Datos de prueba creados exitosamente!");
            System.out.println("🎮 Usuarios creados: " + usuarioRepository.count());
            System.out.println("🎯 Juegos creados: " + juegoRepository.count());
            System.out.println("💰 Compras creadas: " + compraRepository.count());
            System.out.println("\n📧 Credenciales de prueba:");
            System.out.println("   ADMIN:     admin@pixelshop.com / admin123");
            System.out.println("   PROVEEDOR: epic@pixelshop.com / proveedor123");
            System.out.println("   CLIENTE:   maria@gmail.com / cliente123");
        } else {
            System.out.println("ℹ️ DataSeeder: BD ya contiene datos, no se cargan datos de prueba");
        }
    }
    
    private void crearUsuarios() {
        // ADMIN
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail("admin@pixelshop.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        
        // PROVEEDORES
        Usuario proveedor1 = new Usuario();
        proveedor1.setNombre("Epic Games Studio");
        proveedor1.setEmail("epic@pixelshop.com");
        proveedor1.setPassword(passwordEncoder.encode("proveedor123"));
        proveedor1.setRol(Rol.PROVEEDOR);
        proveedor1.setActivo(true);
        proveedor1.setDatosFiscales("CIF: B12345678\nEpic Games Studio S.L.\nCalle Videojuegos 123, Madrid\nTel: 911234567");
        usuarioRepository.save(proveedor1);
        
        Usuario proveedor2 = new Usuario();
        proveedor2.setNombre("Indie Dev Studios");
        proveedor2.setEmail("indie@pixelshop.com");
        proveedor2.setPassword(passwordEncoder.encode("proveedor123"));
        proveedor2.setRol(Rol.PROVEEDOR);
        proveedor2.setActivo(true);
        proveedor2.setDatosFiscales("CIF: B87654321\nIndie Dev Studios S.L.\nAvenida Gaming 456, Barcelona\nTel: 934567890");
        usuarioRepository.save(proveedor2);
        
        // CLIENTES
        Usuario cliente1 = new Usuario();
        cliente1.setNombre("María García");
        cliente1.setEmail("maria@gmail.com");
        cliente1.setPassword(passwordEncoder.encode("cliente123"));
        cliente1.setRol(Rol.CLIENTE);
        cliente1.setActivo(true);
        usuarioRepository.save(cliente1);
        
        Usuario cliente2 = new Usuario();
        cliente2.setNombre("Carlos López");
        cliente2.setEmail("carlos@gmail.com");
        cliente2.setPassword(passwordEncoder.encode("cliente123"));
        cliente2.setRol(Rol.CLIENTE);
        cliente2.setActivo(true);
        usuarioRepository.save(cliente2);
        
        Usuario cliente3 = new Usuario();
        cliente3.setNombre("Ana Martínez");
        cliente3.setEmail("ana@gmail.com");
        cliente3.setPassword(passwordEncoder.encode("cliente123"));
        cliente3.setRol(Rol.CLIENTE);
        cliente3.setActivo(true);
        usuarioRepository.save(cliente3);
    }
    
    private void crearJuegos() {
        Usuario proveedor1 = usuarioRepository.findByEmail("epic@pixelshop.com").get();
        Usuario proveedor2 = usuarioRepository.findByEmail("indie@pixelshop.com").get();
        
        List<Object[]> juegosData = Arrays.asList(
            new Object[]{"Cyberpunk 2077", "Un RPG futurista ambientado en Night City con gráficos impresionantes y una historia envolvente.", "59.99", "RPG", "https://via.placeholder.com/300x400/667eea/ffffff?text=Cyberpunk+2077", proveedor1},
            new Object[]{"The Last of Us Part II", "Aventura post-apocalíptica con una narrativa emotiva y gameplay de supervivencia intenso.", "49.99", "Aventura", "https://via.placeholder.com/300x400/764ba2/ffffff?text=The+Last+of+Us", proveedor1},
            new Object[]{"Hollow Knight", "Metroidvania indie con arte hermoso y combate desafiante en un mundo subterráneo misterioso.", "19.99", "Indie", "https://via.placeholder.com/300x400/f093fb/ffffff?text=Hollow+Knight", proveedor2},
            new Object[]{"FIFA 24", "El simulador de fútbol más realista con equipos actualizados y nuevos modos de juego.", "69.99", "Deportes", "https://via.placeholder.com/300x400/48bb78/ffffff?text=FIFA+24", proveedor1},
            new Object[]{"Stardew Valley", "Simulador de granja relajante donde puedes cultivar, criar animales y formar una comunidad.", "14.99", "Simulación", "https://via.placeholder.com/300x400/4299e1/ffffff?text=Stardew+Valley", proveedor2},
            new Object[]{"Call of Duty: Modern Warfare", "Shooter en primera persona con campaña intensa y multijugador competitivo.", "59.99", "Acción", "https://via.placeholder.com/300x400/e53e3e/ffffff?text=Call+of+Duty", proveedor1},
            new Object[]{"Civilization VI", "Juego de estrategia por turnos donde construyes un imperio que resistirá la prueba del tiempo.", "39.99", "Estrategia", "https://via.placeholder.com/300x400/ed8936/ffffff?text=Civilization+VI", proveedor1},
            new Object[]{"Phasmophobia", "Horror cooperativo donde investigas actividad paranormal con hasta 4 jugadores.", "12.99", "Terror", "https://via.placeholder.com/300x400/1a1a2e/ffffff?text=Phasmophobia", proveedor2},
            new Object[]{"Among Us", "Juego de deducción social donde debes encontrar al impostor entre la tripulación.", "4.99", "Indie", "https://via.placeholder.com/300x400/667eea/ffffff?text=Among+Us", proveedor2},
            new Object[]{"Red Dead Redemption 2", "Western épico con mundo abierto, historia profunda y atención al detalle excepcional.", "59.99", "Aventura", "https://via.placeholder.com/300x400/764ba2/ffffff?text=Red+Dead+2", proveedor1}
        );
        
        int i = 0;
        for (Object[] data : juegosData) {
            Juego juego = new Juego();
            String titulo = (String) data[0];
            juego.setTitulo(titulo);
            juego.setDescripcion((String) data[1]);
            juego.setPrecio(new BigDecimal((String) data[2]));
            juego.setGenero((String) data[3]);
            
            // Establecer URLs de imágenes y video
            juego.setImagenUrl1("https://via.placeholder.com/800x1200/667eea/ffffff?text=Portada+" + (i+1));
            juego.setImagenUrl2("https://via.placeholder.com/1920x1080/764ba2/ffffff?text=Captura+1+" + (i+1));
            juego.setImagenUrl3("https://via.placeholder.com/1920x1080/f093fb/ffffff?text=Captura+2+" + (i+1));
            juego.setImagenUrl4("https://via.placeholder.com/1920x1080/28a745/ffffff?text=Captura+3+" + (i+1));
            juego.setVideoYoutubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"); // Video placeholder
            
            juego.setProveedor((Usuario) data[5]);
            juego.setActivo(true);
            juegoRepository.save(juego);
            i++;
        }
    }
    
    private void crearPublicaciones() {
        List<Juego> juegos = juegoRepository.findAll();
        
        for (Juego juego : juegos) {
            PublicacionJuego publicacion = new PublicacionJuego();
            publicacion.setJuego(juego);
            publicacion.setProveedor(juego.getProveedor());
            publicacion.setPagado(true); // Todas las publicaciones están pagadas
            publicacionRepository.save(publicacion);
        }
    }
    
    private void crearComprasEjemplo() {
        Usuario maria = usuarioRepository.findByEmail("maria@gmail.com").get();
        Usuario carlos = usuarioRepository.findByEmail("carlos@gmail.com").get();
        Usuario ana = usuarioRepository.findByEmail("ana@gmail.com").get();
        
        List<Juego> juegos = juegoRepository.findAll();
        
        // María compra 3 juegos
        crearCompra(maria, juegos.get(0)); // Cyberpunk 2077
        crearCompra(maria, juegos.get(2)); // Hollow Knight
        crearCompra(maria, juegos.get(4)); // Stardew Valley
        
        // Carlos compra 2 juegos
        crearCompra(carlos, juegos.get(1)); // The Last of Us Part II
        crearCompra(carlos, juegos.get(5)); // Call of Duty
        
        // Ana compra 4 juegos
        crearCompra(ana, juegos.get(3)); // FIFA 24
        crearCompra(ana, juegos.get(6)); // Civilization VI
        crearCompra(ana, juegos.get(8)); // Among Us
        crearCompra(ana, juegos.get(9)); // Red Dead Redemption 2
    }
    
    private void crearCompra(Usuario usuario, Juego juego) {
        // VALIDACIÓN: Los administradores NO pueden comprar
        if (usuario.getRol() == Rol.ADMIN) {
            System.out.println("⚠️ ADVERTENCIA: Intento de crear compra para ADMIN - BLOQUEADO");
            return;
        }
        
        // Crear compra
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setPrecioPagado(juego.getPrecio());
        compra.setPagadoAlProveedor(false); // Inicialmente no pagado al proveedor
        compraRepository.save(compra);
        
        // Añadir a biblioteca
        BibliotecaUsuario biblioteca = new BibliotecaUsuario();
        biblioteca.setUsuario(usuario);
        biblioteca.setJuego(juego);
        bibliotecaRepository.save(biblioteca);
        
        // Crear movimiento para el proveedor (85% del precio)
        MovimientoProveedor movimiento = new MovimientoProveedor();
        movimiento.setCompra(compra);
        movimiento.setProveedor(juego.getProveedor());
        
        // Calcular 85% del precio
        BigDecimal montoNeto = juego.getPrecio().multiply(new BigDecimal("0.85"));
        movimiento.setMontoNeto(montoNeto);
        movimiento.setPagado(false); // Pendiente de cobro
        
        movimientoRepository.save(movimiento);
    }
}
