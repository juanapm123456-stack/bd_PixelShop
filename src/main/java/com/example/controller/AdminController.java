package com.example.controller;

import com.example.model.Usuario;
import com.example.model.Compra;
import com.example.service.ServicioUsuario;
import com.example.service.ServicioAdministrador;
import com.example.service.ServicioCompra;
import com.example.service.ServicioAdministrador.ResumenGanancias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/**
 * Controlador para el panel de administración.
 * Solo accesible por usuarios con rol ADMIN.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
    @Autowired
    private ServicioAdministrador servicioAdministrador;
    
    @Autowired
    private ServicioCompra servicioCompra;
    
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = servicioUsuario.listarTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "admin/usuarios";
    }
    
    @PostMapping("/usuario/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        servicioUsuario.eliminarUsuario(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente");
        return "redirect:/admin/usuarios";
    }
    
    @GetMapping("/ganancias")
    public String ganancias(Model model) {
        // Obtener resumen completo de ganancias usando el servicio
        ResumenGanancias resumen = servicioAdministrador.calcularGananciasTotalesPlataforma();
        
        model.addAttribute("gananciaVentas", resumen.getGananciasPorVentas());
        model.addAttribute("gananciaPublicaciones", resumen.getGananciasPorPublicaciones());
        model.addAttribute("gananciaTotal", resumen.getGananciaTotal());
        model.addAttribute("totalVentas", resumen.getTotalVentas());
        model.addAttribute("totalPublicaciones", resumen.getTotalPublicaciones());
        
        return "admin/ganancias";
    }
    
    @GetMapping("/movimientos")
    public String movimientos(Model model) {
        // Obtener solo compras válidas (sin las de administradores)
        List<Compra> compras = servicioCompra.obtenerComprasValidas();
        model.addAttribute("compras", compras);
        return "admin/movimientos";
    }
}
