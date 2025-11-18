package com.example.controller;

import com.example.model.Usuario;
import com.example.model.Rol;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
public class AuthController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registrar(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        // Verificar si email ya existe
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "El email ya está registrado");
            return "redirect:/register";
        }
        
        // Encriptar password
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Por defecto: rol CLIENTE
        usuario.setRol(Rol.CLIENTE);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setActivo(true);
        
        usuarioRepository.save(usuario);
        
        redirectAttributes.addFlashAttribute("success", "Registro exitoso. Inicia sesión");
        return "redirect:/login";
    }
}
