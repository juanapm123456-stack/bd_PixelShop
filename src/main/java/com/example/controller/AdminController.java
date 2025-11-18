package com.example.controller;

import com.example.model.Usuario;
import com.example.model.Compra;
import com.example.repository.UsuarioRepository;
import com.example.repository.CompraRepository;
import com.example.repository.PublicacionJuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Autowired
    private PublicacionJuegoRepository publicacionRepository;
    
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
    
    @PostMapping("/usuario/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente");
        return "redirect:/admin/usuarios";
    }
    
    @GetMapping("/ganancias")
    public String ganancias(Model model) {
        // Calcular ganancias totales
        BigDecimal totalVentas = compraRepository.calcularTotalVentas();
        BigDecimal gananciaVentas = totalVentas != null ? totalVentas.multiply(new BigDecimal("0.15")) : BigDecimal.ZERO;
        
        Long totalPublicaciones = publicacionRepository.contarPublicacionesPagadas();
        BigDecimal gananciaPublicaciones = new BigDecimal(totalPublicaciones != null ? totalPublicaciones : 0)
            .multiply(new BigDecimal("25"));
        
        BigDecimal gananciaTotal = gananciaVentas.add(gananciaPublicaciones);
        
        model.addAttribute("gananciaVentas", gananciaVentas);
        model.addAttribute("gananciaPublicaciones", gananciaPublicaciones);
        model.addAttribute("gananciaTotal", gananciaTotal);
        model.addAttribute("totalVentas", totalVentas);
        model.addAttribute("totalPublicaciones", totalPublicaciones);
        
        return "admin/ganancias";
    }
    
    @GetMapping("/movimientos")
    public String movimientos(Model model) {
        List<Compra> compras = compraRepository.findAll();
        model.addAttribute("compras", compras);
        return "admin/movimientos";
    }
}
