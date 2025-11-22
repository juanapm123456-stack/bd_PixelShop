package com.example.controller;

import com.example.model.*;
import com.example.repository.*;
import com.example.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/compra")
public class CompraController extends BaseController {
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private BibliotecaUsuarioRepository bibliotecaRepository;
    
    @Autowired
    private MovimientoProveedorRepository movimientoRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Confirma la compra y la registra en la base de datos
     */
    @GetMapping("/confirmar/{juegoId}")
    public String confirmarCompra(@PathVariable Long juegoId, 
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // ✅ VALIDACIÓN CRÍTICA: ADMIN NO PUEDE COMPRAR
        if (usuario.getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
            return "redirect:/juego/" + juegoId;
        }
        
        Juego juego = juegoRepository.findById(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // Verificar que no haya comprado ya este juego
        if (compraRepository.existsByUsuarioAndJuego(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/juego/" + juegoId;
        }
        
        // Crear COMPRA
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setPrecioPagado(juego.getPrecio());
        compra.setFechaCompra(LocalDateTime.now());
        compra.setPagadoAlProveedor(false);
        compraRepository.save(compra);
        
        // Añadir a BIBLIOTECA
        BibliotecaUsuario biblioteca = new BibliotecaUsuario();
        biblioteca.setUsuario(usuario);
        biblioteca.setJuego(juego);
        biblioteca.setFechaAdquisicion(LocalDateTime.now());
        bibliotecaRepository.save(biblioteca);
        
        // Crear MOVIMIENTO_PROVEEDOR (85% al proveedor)
        BigDecimal montoProveedor = juego.getPrecio().multiply(new BigDecimal("0.85"));
        MovimientoProveedor movimiento = new MovimientoProveedor();
        movimiento.setProveedor(juego.getProveedor());
        movimiento.setCompra(compra);
        movimiento.setMontoNeto(montoProveedor);
        movimiento.setPagado(false);
        movimiento.setFecha(LocalDateTime.now());
        movimientoRepository.save(movimiento);
        
        // Enviar email de confirmación
        try {
            System.out.println("📧 Intentando enviar email de confirmación a: " + usuario.getEmail());
            emailService.enviarConfirmacionCompra(compra);
            System.out.println("✅ Email enviado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email de confirmación: " + e.getMessage());
            e.printStackTrace();
        }
        
        redirectAttributes.addFlashAttribute("success", "¡Compra realizada! El juego está en tu biblioteca");
        return "redirect:/mi-biblioteca";
    }
    
    @PostMapping("/{juegoId}")
    public String comprarJuego(@PathVariable Long juegoId, 
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        // Verificar que el usuario esté autenticado
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para comprar");
            return "redirect:/login";
        }
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Verificar que no sea ADMIN
        if (usuario.getRol() == Rol.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
            return "redirect:/juego/" + juegoId;
        }
        
        // Verificar que el juego existe
        Juego juego = juegoRepository.findById(juegoId)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // Verificar que no lo haya comprado ya
        if (compraRepository.existsByUsuarioAndJuego(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/mi-biblioteca";
        }
        
        // Redirigir al checkout para seleccionar método de pago
        return "redirect:/paypal/checkout/" + juegoId;
    }
    
    @GetMapping("/mis-compras")
    public String misCompras(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Compra> compras = compraRepository.findByUsuarioOrderByFechaCompraDesc(usuario);
        model.addAttribute("compras", compras);
        
        return "usuario/mis-compras";
    }
}
