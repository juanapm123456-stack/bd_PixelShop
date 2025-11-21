package com.example.controller;

import com.example.model.BibliotecaUsuario;
import com.example.model.Usuario;
import com.example.service.ServicioUsuario;
import com.example.service.ServicioBiblioteca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

/**
 * Controlador para gestionar la biblioteca de juegos del usuario.
 */
@Controller
public class BibliotecaController {
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
    @Autowired
    private ServicioBiblioteca servicioBiblioteca;
    
    @GetMapping("/mi-biblioteca")
    public String miBiblioteca(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener usuario logueado
        Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // Obtener todos los juegos de su biblioteca
        List<BibliotecaUsuario> biblioteca = servicioBiblioteca.obtenerBibliotecaDeUsuario(usuario);
        model.addAttribute("biblioteca", biblioteca);
        
        return "usuario/mi-biblioteca";
    }
}
