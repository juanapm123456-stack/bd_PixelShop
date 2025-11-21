package com.example.config;

import com.example.model.*;
import com.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Carga datos de EJEMPLO para demostración.
 * Se ejecuta SIEMPRE (desarrollo y producción).
 * 
 * Incluye:
 * - 1 Administrador
 * - 2 Proveedores
 * - 3 Clientes
 * - 10 Juegos de ejemplo
 * - Compras y movimientos de ejemplo
 * 
 * NOTA: Estos son datos de DEMOSTRACIÓN con contraseñas simples.
 * En producción real, cambiar las contraseñas por unas seguras.
 */
@Component
@Order(1)  // Se ejecuta primero
public class EjemplosDataSeeder implements CommandLineRunner {

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
        if (usuarioRepository.count() == 0) {
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║  📚 CARGANDO DATOS DE EJEMPLO PARA DEMOSTRACIÓN             ║");
            System.out.println("║  ⚠️  Estos datos son para PRUEBAS y DEMOSTRACIÓN            ║");
            System.out.println("║  🔐 En producción: CAMBIAR las contraseñas por seguras      ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            // 1. CREAR USUARIOS DE EJEMPLO
            crearUsuariosEjemplo();
            
            // 2. CREAR JUEGOS DE EJEMPLO
            crearJuegosEjemplo();
            
            // 3. CREAR PUBLICACIONES DE EJEMPLO
            crearPublicacionesEjemplo();
            
            // 4. CREAR COMPRAS DE EJEMPLO
            crearComprasEjemplo();
            
            System.out.println("\n✅ Datos de ejemplo creados exitosamente!");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("📊 RESUMEN:");
            System.out.println("   👥 Usuarios: " + usuarioRepository.count());
            System.out.println("   🎮 Juegos: " + juegoRepository.count());
            System.out.println("   💰 Compras: " + compraRepository.count());
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("🔑 CREDENCIALES DE EJEMPLO:");
            System.out.println("   ADMIN:     admin@pixelshop.com / admin123");
            System.out.println("   PROVEEDOR: epic@pixelshop.com / proveedor123");
            System.out.println("   CLIENTE:   maria@gmail.com / cliente123");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("⚠️  RECUERDA: Cambiar estas contraseñas en producción real");
            System.out.println("═══════════════════════════════════════════════════════════════\n");
        } else {
            System.out.println("ℹ️  EjemplosDataSeeder: BD ya contiene datos, no se cargan ejemplos");
        }
    }
    
    private void crearUsuariosEjemplo() {
        // ========== ADMINISTRADOR DE EJEMPLO ==========
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail("admin@pixelshop.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);
        System.out.println("   ✅ Admin creado: admin@pixelshop.com");
        
        // ========== PROVEEDORES DE EJEMPLO ==========
        Usuario proveedor1 = new Usuario();
        proveedor1.setNombre("Epic Games Studio");
        proveedor1.setEmail("epic@pixelshop.com");
        proveedor1.setPassword(passwordEncoder.encode("proveedor123"));
        proveedor1.setRol(Rol.PROVEEDOR);
        proveedor1.setActivo(true);
        proveedor1.setDatosFiscales("CIF: B12345678\nEpic Games Studio S.L.\nCalle Videojuegos 123, Madrid\nTel: 911234567");
        usuarioRepository.save(proveedor1);
        System.out.println("   ✅ Proveedor creado: epic@pixelshop.com");
        
        Usuario proveedor2 = new Usuario();
        proveedor2.setNombre("Indie Dev Studios");
        proveedor2.setEmail("indie@pixelshop.com");
        proveedor2.setPassword(passwordEncoder.encode("proveedor123"));
        proveedor2.setRol(Rol.PROVEEDOR);
        proveedor2.setActivo(true);
        proveedor2.setDatosFiscales("CIF: B87654321\nIndie Dev Studios S.L.\nAvenida Gaming 456, Barcelona\nTel: 934567890");
        usuarioRepository.save(proveedor2);
        System.out.println("   ✅ Proveedor creado: indie@pixelshop.com");
        
        // ========== CLIENTES DE EJEMPLO ==========
        Usuario cliente1 = new Usuario();
        cliente1.setNombre("María García");
        cliente1.setEmail("maria@gmail.com");
        cliente1.setPassword(passwordEncoder.encode("cliente123"));
        cliente1.setRol(Rol.CLIENTE);
        cliente1.setActivo(true);
        usuarioRepository.save(cliente1);
        System.out.println("   ✅ Cliente creado: maria@gmail.com");
        
        Usuario cliente2 = new Usuario();
        cliente2.setNombre("Carlos López");
        cliente2.setEmail("carlos@gmail.com");
        cliente2.setPassword(passwordEncoder.encode("cliente123"));
        cliente2.setRol(Rol.CLIENTE);
        cliente2.setActivo(true);
        usuarioRepository.save(cliente2);
        System.out.println("   ✅ Cliente creado: carlos@gmail.com");
        
        Usuario cliente3 = new Usuario();
        cliente3.setNombre("Ana Martínez");
        cliente3.setEmail("ana@gmail.com");
        cliente3.setPassword(passwordEncoder.encode("cliente123"));
        cliente3.setRol(Rol.CLIENTE);
        cliente3.setActivo(true);
        usuarioRepository.save(cliente3);
        System.out.println("   ✅ Cliente creado: ana@gmail.com");
    }
    
    private void crearJuegosEjemplo() {
        Usuario proveedor1 = usuarioRepository.findByEmail("epic@pixelshop.com").get();
        Usuario proveedor2 = usuarioRepository.findByEmail("indie@pixelshop.com").get();
        
        System.out.println("\n   🎮 Creando juegos de ejemplo...");
        
        List<Object[]> juegosData = Arrays.asList(
            new Object[]{"Cyberpunk 2077", "Un RPG futurista ambientado en Night City con gráficos impresionantes y una historia envolvente. Explora un mundo abierto lleno de misiones y personajes memorables.", "59.99", "RPG", proveedor1},
            new Object[]{"The Last of Us Part II", "Aventura post-apocalíptica con una narrativa emotiva y gameplay de supervivencia intenso. Una historia sobre venganza, redención y humanidad.", "49.99", "Aventura", proveedor1},
            new Object[]{"Hollow Knight", "Metroidvania indie con arte hermoso y combate desafiante en un mundo subterráneo misterioso. Explora cavernas oscuras y enfrenta criaturas peligrosas.", "19.99", "Indie", proveedor2},
            new Object[]{"FIFA 24", "El simulador de fútbol más realista con equipos actualizados y nuevos modos de juego. Juega con tus equipos favoritos y domina el campo.", "69.99", "Deportes", proveedor1},
            new Object[]{"Stardew Valley", "Simulador de granja relajante donde puedes cultivar, criar animales y formar una comunidad. Escapa de la vida urbana y construye tu granja de ensueño.", "14.99", "Simulación", proveedor2},
            new Object[]{"Call of Duty: Modern Warfare", "Shooter en primera persona con campaña intensa y multijugador competitivo. Acción trepidante y combates realistas.", "59.99", "Acción", proveedor1},
            new Object[]{"Civilization VI", "Juego de estrategia por turnos donde construyes un imperio que resistirá la prueba del tiempo. Lidera tu civilización a la victoria.", "39.99", "Estrategia", proveedor1},
            new Object[]{"Phasmophobia", "Horror cooperativo donde investigas actividad paranormal con hasta 4 jugadores. ¿Te atreves a enfrentar los fantasmas?", "12.99", "Terror", proveedor2},
            new Object[]{"Among Us", "Juego de deducción social donde debes encontrar al impostor entre la tripulación. Coopera o engaña para ganar.", "4.99", "Indie", proveedor2},
            new Object[]{"Red Dead Redemption 2", "Western épico con mundo abierto, historia profunda y atención al detalle excepcional. Vive la vida de un forajido en el salvaje oeste.", "59.99", "Aventura", proveedor1}
        );
        
        int i = 0;
        for (Object[] data : juegosData) {
            Juego juego = new Juego();
            String titulo = (String) data[0];
            juego.setTitulo(titulo);
            juego.setDescripcion((String) data[1]);
            juego.setPrecio(new BigDecimal((String) data[2]));
            juego.setGenero((String) data[3]);
            
            // URLs de imágenes de ejemplo (placeholders)
            juego.setImagenUrl1("https://via.placeholder.com/800x1200/667eea/ffffff?text=Portada+" + (i+1));
            juego.setImagenUrl2("https://via.placeholder.com/1920x1080/764ba2/ffffff?text=Captura+1+" + (i+1));
            juego.setImagenUrl3("https://via.placeholder.com/1920x1080/f093fb/ffffff?text=Captura+2+" + (i+1));
            juego.setImagenUrl4("https://via.placeholder.com/1920x1080/28a745/ffffff?text=Captura+3+" + (i+1));
            juego.setVideoYoutubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            
            juego.setProveedor((Usuario) data[4]);
            juego.setActivo(true);
            juegoRepository.save(juego);
            System.out.println("      ✓ " + titulo + " - " + data[2] + "€");
            i++;
        }
    }
    
    private void crearPublicacionesEjemplo() {
        System.out.println("\n   📤 Registrando publicaciones (25€ cada una)...");
        
        List<Juego> juegos = juegoRepository.findAll();
        
        for (Juego juego : juegos) {
            PublicacionJuego publicacion = new PublicacionJuego();
            publicacion.setJuego(juego);
            publicacion.setProveedor(juego.getProveedor());
            publicacion.setPagado(true); // Todas las publicaciones están pagadas
            publicacionRepository.save(publicacion);
        }
        
        BigDecimal totalPublicaciones = new BigDecimal("25.00")
            .multiply(new BigDecimal(juegos.size()));
        System.out.println("      ✓ " + juegos.size() + " publicaciones registradas");
        System.out.println("      💰 Ingresos por publicaciones: " + totalPublicaciones + "€");
    }
    
    private void crearComprasEjemplo() {
        System.out.println("\n   🛒 Creando compras de ejemplo...");
        
        Usuario maria = usuarioRepository.findByEmail("maria@gmail.com").get();
        Usuario carlos = usuarioRepository.findByEmail("carlos@gmail.com").get();
        Usuario ana = usuarioRepository.findByEmail("ana@gmail.com").get();
        
        List<Juego> juegos = juegoRepository.findAll();
        
        // María compra 3 juegos
        crearCompraEjemplo(maria, juegos.get(0)); // Cyberpunk 2077
        crearCompraEjemplo(maria, juegos.get(2)); // Hollow Knight
        crearCompraEjemplo(maria, juegos.get(4)); // Stardew Valley
        
        // Carlos compra 2 juegos
        crearCompraEjemplo(carlos, juegos.get(1)); // The Last of Us Part II
        crearCompraEjemplo(carlos, juegos.get(5)); // Call of Duty
        
        // Ana compra 4 juegos
        crearCompraEjemplo(ana, juegos.get(3)); // FIFA 24
        crearCompraEjemplo(ana, juegos.get(6)); // Civilization VI
        crearCompraEjemplo(ana, juegos.get(8)); // Among Us
        crearCompraEjemplo(ana, juegos.get(9)); // Red Dead Redemption 2
        
        // Calcular totales
        BigDecimal totalVentas = compraRepository.findAll().stream()
            .map(Compra::getPrecioPagado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal comisionPlataforma = totalVentas.multiply(new BigDecimal("0.15"));
        BigDecimal comisionProveedores = totalVentas.multiply(new BigDecimal("0.85"));
        
        System.out.println("\n   📊 RESUMEN DE COMPRAS:");
        System.out.println("      💵 Total ventas: " + totalVentas + "€");
        System.out.println("      🏢 Comisión plataforma (15%): " + comisionPlataforma + "€");
        System.out.println("      👨‍💼 Para proveedores (85%): " + comisionProveedores + "€");
    }
    
    private void crearCompraEjemplo(Usuario usuario, Juego juego) {
        // VALIDACIÓN: Los administradores NO pueden comprar
        if (usuario.getRol() == Rol.ADMIN) {
            System.out.println("      ⚠️ BLOQUEADO: ADMIN no puede comprar");
            return;
        }
        
        // Crear compra
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setPrecioPagado(juego.getPrecio());
        compra.setPagadoAlProveedor(false);
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
        BigDecimal montoNeto = juego.getPrecio().multiply(new BigDecimal("0.85"));
        movimiento.setMontoNeto(montoNeto);
        movimiento.setPagado(false);
        movimientoRepository.save(movimiento);
        
        System.out.println("      ✓ " + usuario.getNombre() + " compró " + juego.getTitulo() + 
                           " - " + juego.getPrecio() + "€ (85% → proveedor: " + montoNeto + "€)");
    }
}
