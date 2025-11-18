package com.example.controller;

import com.example.model.*;
import com.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
public class CompraController {
    
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
    
    @PostMapping("/{juegoId}")
    public String comprarJuego(@PathVariable Long juegoId, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
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
        
        redirectAttributes.addFlashAttribute("success", "¡Compra realizada! El juego está en tu biblioteca");
        return "redirect:/mi-biblioteca";
    }
    
    @GetMapping("/mis-compras")
    public String misCompras(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Compra> compras = compraRepository.findByUsuarioOrderByFechaCompraDesc(usuario);
        model.addAttribute("compras", compras);
        
        return "usuario/mis-compras";
    }
}
