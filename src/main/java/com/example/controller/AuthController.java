package com.example.controller;

import com.example.model.Usuario;
import com.example.service.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para gestionar autenticación y registro de usuarios.
 */
@Controller
public class AuthController {
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
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
        try {
            // Registrar nuevo usuario usando el servicio
            servicioUsuario.registrarNuevoUsuario(usuario);
            redirectAttributes.addFlashAttribute("success", "Registro exitoso. Inicia sesión");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
