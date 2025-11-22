package com.example.controller;

import com.example.model.Rol;
import com.example.model.Usuario;
import com.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController extends BaseController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private BibliotecaUsuarioRepository bibliotecaRepository;
    
    @Autowired
    private CompraRepository compraRepository;
    
    @Autowired
    private MovimientoProveedorRepository movimientoRepository;
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    @GetMapping
    public String mostrarPerfil(Model model, Authentication authentication) {
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        return "usuario/perfil";
    }
    
    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioActualizado,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        
        String email = obtenerEmailDelUsuario(authentication);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        usuario.setNombre(usuarioActualizado.getNombre());
        usuario.setDatosFiscales(usuarioActualizado.getDatosFiscales());
        
        // Si es proveedor, permitir actualizar email PayPal
        if (usuario.getRol() == Rol.PROVEEDOR && usuarioActualizado.getEmailPayPal() != null) {
            usuario.setEmailPayPal(usuarioActualizado.getEmailPayPal());
        }
        
        usuarioRepository.save(usuario);
        
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        return "redirect:/perfil";
    }
    
    @DeleteMapping("/eliminar")
    @ResponseBody
    public String eliminarCuenta(Authentication authentication) {
        try {
            String email = obtenerEmailDelUsuario(authentication);
            Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            System.out.println("🗑️ Eliminando cuenta del usuario: " + usuario.getEmail());
            
            // 1. Eliminar entradas de biblioteca
            bibliotecaRepository.deleteByUsuario(usuario);
            System.out.println("✅ Biblioteca eliminada");
            
            // 2. Eliminar movimientos como proveedor
            movimientoRepository.deleteByProveedor(usuario);
            System.out.println("✅ Movimientos de proveedor eliminados");
            
            // 3. Eliminar compras (esto también afecta movimientos relacionados)
            compraRepository.deleteByUsuario(usuario);
            System.out.println("✅ Compras eliminadas");
            
            // 4. Eliminar juegos publicados (si es proveedor)
            juegoRepository.deleteByProveedor(usuario);
            System.out.println("✅ Juegos publicados eliminados");
            
            // 5. Finalmente eliminar usuario
            usuarioRepository.delete(usuario);
            System.out.println("✅ Usuario eliminado correctamente");
            
            return "OK";
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar cuenta: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar cuenta: " + e.getMessage());
        }
    }
}
