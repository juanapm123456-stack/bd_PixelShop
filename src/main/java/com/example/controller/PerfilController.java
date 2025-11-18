package com.example.controller;

import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @GetMapping
    public String mostrarPerfil(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "usuario/perfil";
    }
    
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioActualizado,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setDatosFiscales(usuarioActualizado.getDatosFiscales());
        
        usuarioRepository.save(usuario);
        
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }
}
