package com.example.controller;

import com.example.model.Juego;
import com.example.model.Usuario;
import com.example.service.ServicioJuego;
import com.example.service.ServicioUsuario;
import com.example.service.ServicioCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * Controlador para mostrar el catálogo de juegos.
 */
@Controller
public class CatalogoController {
    
    @Autowired
    private ServicioJuego servicioJuego;
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
    @Autowired
    private ServicioCompra servicioCompra;
    
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener todos los juegos activos del catálogo
        List<Juego> juegos = servicioJuego.obtenerJuegosActivos();
        model.addAttribute("juegos", juegos);
        
        // Si hay usuario logueado, obtener sus juegos comprados (solo CLIENTE y PROVEEDOR)
        if (userDetails != null) {
            Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
            
            // Solo obtener juegos comprados si no es administrador
            if (servicioCompra.puedeRealizarCompras(usuario)) {
                List<Long> juegosCompradosIds = servicioCompra.obtenerIdsDeJuegosComprados(usuario);
                model.addAttribute("juegosComprados", juegosCompradosIds);
            }
        }
        
        return "catalogo/index";
    }
    
    @GetMapping("/juego/{id}")
    public String detalleJuego(@PathVariable Long id, Model model, 
                               @AuthenticationPrincipal UserDetails userDetails) {
        // Buscar el juego por ID
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        model.addAttribute("juego", juego);
        
        // Verificar si el usuario ya compró este juego (solo para CLIENTE y PROVEEDOR)
        if (userDetails != null) {
            Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
            
            // Los ADMIN nunca pueden "haber comprado" porque no pueden comprar
            boolean yaComprado = false;
            if (servicioCompra.puedeRealizarCompras(usuario)) {
                yaComprado = servicioCompra.verificarJuegoYaComprado(usuario, juego);
            }
            model.addAttribute("yaComprado", yaComprado);
        }
        
        return "catalogo/juego-detalle";
    }
    
    @GetMapping("/buscar")
    public String buscar(@RequestParam String q, Model model) {
        // Buscar juegos por título
        List<Juego> juegos = servicioJuego.buscarJuegosPorTitulo(q);
        model.addAttribute("juegos", juegos);
        model.addAttribute("busqueda", q);
        return "catalogo/index";
    }
}
