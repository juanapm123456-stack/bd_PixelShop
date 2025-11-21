package com.example.controller;

import com.example.model.*;
import com.example.service.*;
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
import java.util.List;

/**
 * Controlador para gestionar la compra de juegos.
 */
@Controller
@RequestMapping("/compra")
public class CompraController {
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
    @Autowired
    private ServicioJuego servicioJuego;
    
    @Autowired
    private ServicioCompra servicioCompra;
    
    @Autowired
    private ServicioBiblioteca servicioBiblioteca;
    
    @Autowired
    private ServicioProveedor servicioProveedor;
    
    @PostMapping("/{juegoId}")
    public String comprarJuego(@PathVariable Long juegoId, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        // Obtener usuario logueado
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // ✅ VALIDACIÓN CRÍTICA: Verificar si el usuario puede comprar
        if (!servicioCompra.puedeRealizarCompras(usuario)) {
            redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
            return "redirect:/juego/" + juegoId;
        }
        
        // Obtener el juego a comprar
        Juego juego = servicioJuego.buscarJuegoPorId(juegoId);
        
        // Verificar que no haya comprado ya este juego
        if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
            return "redirect:/juego/" + juegoId;
        }
        
        // 1. Crear y guardar la COMPRA
        Compra compra = servicioCompra.crearNuevaCompra(usuario, juego);
        
        // 2. Añadir el juego a la BIBLIOTECA del usuario
        servicioBiblioteca.agregarJuegoABiblioteca(usuario, juego);
        
        // 3. Crear MOVIMIENTO_PROVEEDOR (calcular 85% para el proveedor)
        BigDecimal montoProveedor = servicioCompra.calcularMontoParaProveedor(juego.getPrecio());
        servicioProveedor.crearMovimientoProveedor(juego.getProveedor(), compra, montoProveedor);
        
        redirectAttributes.addFlashAttribute("success", "¡Compra realizada! El juego está en tu biblioteca");
        return "redirect:/mi-biblioteca";
    }
    
    @GetMapping("/mis-compras")
    public String misCompras(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener usuario logueado
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // Obtener todas las compras del usuario
        List<Compra> compras = servicioCompra.obtenerComprasDeUsuario(usuario);
        model.addAttribute("compras", compras);
        
        return "usuario/mis-compras";
    }
}
