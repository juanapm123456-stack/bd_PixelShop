package com.example.controller;

import com.example.model.Juego;
import com.example.model.Usuario;
import com.example.model.Compra;
import com.example.repository.JuegoRepository;
import com.example.repository.CompraRepository;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CatalogoController {
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        List<Juego> juegos = juegoRepository.findByActivoTrue();
        model.addAttribute("juegos", juegos);
        
        // Si hay usuario logueado, obtener sus juegos comprados
        if (userDetails != null) {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (usuario != null) {
                List<Compra> compras = compraRepository.findByUsuarioOrderByFechaCompraDesc(usuario);
                List<Long> juegosCompradosIds = compras.stream()
                    .map(c -> c.getJuego().getId())
                    .collect(Collectors.toList());
                model.addAttribute("juegosComprados", juegosCompradosIds);
            }
        }
        
        return "catalogo/index";
    }
    
    @GetMapping("/juego/{id}")
    public String detalleJuego(@PathVariable Long id, Model model, 
                               @AuthenticationPrincipal UserDetails userDetails) {
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        model.addAttribute("juego", juego);
        
        // Verificar si el usuario ya compró este juego
        if (userDetails != null) {
            Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername()).orElse(null);
            if (usuario != null) {
                boolean yaComprado = compraRepository.existsByUsuarioAndJuego(usuario, juego);
                model.addAttribute("yaComprado", yaComprado);
            }
        }
        
        return "catalogo/juego-detalle";
    }
    
    @GetMapping("/buscar")
    public String buscar(@RequestParam String q, Model model) {
        List<Juego> juegos = juegoRepository.findByTituloContainingIgnoreCaseAndActivoTrue(q);
        model.addAttribute("juegos", juegos);
        model.addAttribute("busqueda", q);
        return "catalogo/index";
    }
}
