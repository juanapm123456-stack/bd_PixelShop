package com.example.controller;

import com.example.model.BibliotecaUsuario;
import com.example.model.Usuario;
import com.example.repository.BibliotecaUsuarioRepository;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class BibliotecaController extends BaseController {
    
    @Autowired
    private BibliotecaUsuarioRepository bibliotecaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @GetMapping("/mi-biblioteca")
    public String miBiblioteca(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<BibliotecaUsuario> biblioteca = bibliotecaRepository.findByUsuarioOrderByFechaAdquisicionDesc(usuario);
        model.addAttribute("biblioteca", biblioteca);
        
        return "usuario/mi-biblioteca";
    }
}
