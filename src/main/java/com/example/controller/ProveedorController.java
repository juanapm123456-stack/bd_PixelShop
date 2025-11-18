package com.example.controller;

import com.example.model.*;
import com.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/proveedor")
@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
public class ProveedorController {
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PublicacionJuegoRepository publicacionRepository;
    
    @Autowired
    private MovimientoProveedorRepository movimientoRepository;
    
    @GetMapping("/publicar")
    public String mostrarFormularioPublicar(Model model) {
        model.addAttribute("juego", new Juego());
        return "proveedor/publicar-juego";
    }
    
    @PostMapping("/publicar")
    public String publicarJuego(@ModelAttribute Juego juego,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Configurar juego
        juego.setProveedor(proveedor);
        juego.setFechaPublicacion(LocalDateTime.now());
        juego.setActivo(true);
        juegoRepository.save(juego);
        
        // Registrar PUBLICACION (cobro de 25€)
        PublicacionJuego publicacion = new PublicacionJuego();
        publicacion.setJuego(juego);
        publicacion.setProveedor(proveedor);
        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setPagado(true); // Simular pago de 25€
        publicacionRepository.save(publicacion);
        
        redirectAttributes.addFlashAttribute("success", "¡Juego publicado exitosamente! (25€ cobrados)");
        return "redirect:/proveedor/mis-juegos";
    }
    
    @GetMapping("/mis-juegos")
    public String misJuegos(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Juego> juegos = juegoRepository.findByProveedor(proveedor);
        model.addAttribute("juegos", juegos);
        
        return "proveedor/mis-juegos";
    }
    
    @GetMapping("/ventas")
    public String ventas(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<MovimientoProveedor> movimientos = movimientoRepository.findByProveedorOrderByFechaDesc(proveedor);
        BigDecimal ingresosPendientes = movimientoRepository.calcularIngresosPendientes(proveedor);
        
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("ingresosPendientes", ingresosPendientes != null ? ingresosPendientes : BigDecimal.ZERO);
        
        return "proveedor/ventas";
    }
    
    @PostMapping("/cobrar/{movimientoId}")
    public String cobrarMovimiento(@PathVariable Long movimientoId, RedirectAttributes redirectAttributes) {
        MovimientoProveedor movimiento = movimientoRepository.findById(movimientoId)
            .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        
        movimiento.setPagado(true);
        movimientoRepository.save(movimiento);
        
        redirectAttributes.addFlashAttribute("success", "Movimiento marcado como cobrado");
        return "redirect:/proveedor/ventas";
    }
}
