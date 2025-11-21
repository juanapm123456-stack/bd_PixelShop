package com.example.controller;

import com.example.model.*;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.io.IOException;

/**
 * Controlador para gestionar operaciones de proveedores.
 * Los proveedores pueden publicar juegos, ver sus ventas y cobrar sus ganancias.
 */
@Controller
@RequestMapping("/proveedor")
@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
public class ProveedorController {
    
    @Autowired
    private ServicioUsuario servicioUsuario;
    
    @Autowired
    private ServicioJuego servicioJuego;
    
    @Autowired
    private ServicioProveedor servicioProveedor;
    
    @GetMapping("/publicar")
    public String mostrarFormularioPublicar(Model model) {
        model.addAttribute("juego", new Juego());
        return "proveedor/publicar-juego";
    }
    
    @PostMapping("/publicar")
    public String publicarJuego(@ModelAttribute Juego juego,
                                @RequestParam("imagen1") MultipartFile imagen1,
                                @RequestParam("imagen2") MultipartFile imagen2,
                                @RequestParam("imagen3") MultipartFile imagen3,
                                @RequestParam("imagen4") MultipartFile imagen4,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        
        // Obtener el proveedor logueado
        Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        try {
            // Validar las 4 imágenes
            String validacion1 = servicioJuego.validarImagen(imagen1, "Imagen 1");
            if (validacion1 != null) {
                redirectAttributes.addFlashAttribute("error", validacion1);
                return "redirect:/proveedor/publicar";
            }
            
            String validacion2 = servicioJuego.validarImagen(imagen2, "Imagen 2");
            if (validacion2 != null) {
                redirectAttributes.addFlashAttribute("error", validacion2);
                return "redirect:/proveedor/publicar";
            }
            
            String validacion3 = servicioJuego.validarImagen(imagen3, "Imagen 3");
            if (validacion3 != null) {
                redirectAttributes.addFlashAttribute("error", validacion3);
                return "redirect:/proveedor/publicar";
            }
            
            String validacion4 = servicioJuego.validarImagen(imagen4, "Imagen 4");
            if (validacion4 != null) {
                redirectAttributes.addFlashAttribute("error", validacion4);
                return "redirect:/proveedor/publicar";
            }
            
            // Guardar las imágenes en el servidor
            if (!imagen1.isEmpty()) {
                String urlImagen1 = servicioJuego.guardarImagenJuego(imagen1, 1);
                juego.setImagenUrl1(urlImagen1);
            }
            
            if (!imagen2.isEmpty()) {
                String urlImagen2 = servicioJuego.guardarImagenJuego(imagen2, 2);
                juego.setImagenUrl2(urlImagen2);
            }
            
            if (!imagen3.isEmpty()) {
                String urlImagen3 = servicioJuego.guardarImagenJuego(imagen3, 3);
                juego.setImagenUrl3(urlImagen3);
            }
            
            if (!imagen4.isEmpty()) {
                String urlImagen4 = servicioJuego.guardarImagenJuego(imagen4, 4);
                juego.setImagenUrl4(urlImagen4);
            }
            
            // Validar que se hayan subido las 4 imágenes
            if (juego.getImagenUrl1() == null || juego.getImagenUrl2() == null || 
                juego.getImagenUrl3() == null || juego.getImagenUrl4() == null) {
                redirectAttributes.addFlashAttribute("error", "Debes subir las 4 imágenes obligatorias (1 portada + 3 capturas)");
                return "redirect:/proveedor/publicar";
            }
            
            // Configurar y guardar el juego
            juego.setProveedor(proveedor);
            servicioJuego.guardarJuego(juego);
            
            // Registrar la publicación (cobro de 25€)
            servicioProveedor.registrarPublicacionJuego(juego, proveedor);
            
            redirectAttributes.addFlashAttribute("success", "¡Juego publicado exitosamente! (25€ cobrados)");
            return "redirect:/proveedor/mis-juegos";
            
        } catch (IOException e) {
            String errorMessage = "Error al procesar las imágenes. ";
            if (e.getMessage().contains("No space left")) {
                errorMessage += "No hay espacio suficiente en el servidor.";
            } else if (e.getMessage().contains("Access denied") || e.getMessage().contains("Permission")) {
                errorMessage += "No se tienen permisos para guardar archivos.";
            } else if (e.getMessage().contains("File too large")) {
                errorMessage += "Una o más imágenes superan el tamaño máximo de 10MB.";
            } else {
                errorMessage += "Verifica que las imágenes sean válidas (JPG, PNG, WEBP) y no superen 10MB cada una.";
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/proveedor/publicar";
        }
    }
    
    @GetMapping("/mis-juegos")
    public String misJuegos(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener proveedor logueado
        Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // Obtener todos sus juegos publicados
        List<Juego> juegos = servicioJuego.obtenerJuegosDeProveedor(proveedor);
        model.addAttribute("juegos", juegos);
        
        return "proveedor/mis-juegos";
    }
    
    @GetMapping("/ventas")
    public String ventas(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener proveedor logueado
        Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // Obtener todos sus movimientos financieros
        List<MovimientoProveedor> movimientos = servicioProveedor.obtenerMovimientosDeProveedor(proveedor);
        
        // Calcular total de ingresos pendientes
        BigDecimal ingresosPendientes = servicioProveedor.calcularIngresosPendientes(proveedor);
        
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("ingresosPendientes", ingresosPendientes);
        
        return "proveedor/ventas";
    }
    
    @PostMapping("/cobrar/{movimientoId}")
    public String cobrarMovimiento(@PathVariable Long movimientoId, RedirectAttributes redirectAttributes) {
        // Marcar el movimiento como cobrado
        servicioProveedor.marcarMovimientoComoCobrado(movimientoId);
        
        redirectAttributes.addFlashAttribute("success", "Movimiento marcado como cobrado");
        return "redirect:/proveedor/ventas";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable Long id, Model model, 
                                @AuthenticationPrincipal UserDetails userDetails) {
        // Obtener proveedor logueado
        Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // Obtener el juego a editar
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // VALIDAR: Solo el proveedor dueño puede editar
        if (!servicioJuego.esProveedorDelJuego(juego, proveedor)) {
            return "redirect:/proveedor/mis-juegos";
        }
        
        model.addAttribute("juego", juego);
        return "proveedor/editar-juego";
    }
    
    @PostMapping("/editar/{id}")
    public String editarJuego(@PathVariable Long id, 
                              @ModelAttribute Juego juegoActualizado,
                              @RequestParam(value = "imagen1", required = false) MultipartFile imagen1,
                              @RequestParam(value = "imagen2", required = false) MultipartFile imagen2,
                              @RequestParam(value = "imagen3", required = false) MultipartFile imagen3,
                              @RequestParam(value = "imagen4", required = false) MultipartFile imagen4,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        
        // Obtener proveedor logueado
        Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(userDetails.getUsername());
        
        // Obtener el juego a editar
        Juego juego = servicioJuego.buscarJuegoPorId(id);
        
        // VALIDAR: Solo el proveedor dueño puede editar
        if (!servicioJuego.esProveedorDelJuego(juego, proveedor)) {
            return "redirect:/proveedor/mis-juegos";
        }
        
        try {
            // Actualizar imágenes solo si se suben nuevas
            if (imagen1 != null && !imagen1.isEmpty()) {
                String urlImagen1 = servicioJuego.guardarImagenJuego(imagen1, 1);
                juego.setImagenUrl1(urlImagen1);
            }
            
            if (imagen2 != null && !imagen2.isEmpty()) {
                String urlImagen2 = servicioJuego.guardarImagenJuego(imagen2, 2);
                juego.setImagenUrl2(urlImagen2);
            }
            
            if (imagen3 != null && !imagen3.isEmpty()) {
                String urlImagen3 = servicioJuego.guardarImagenJuego(imagen3, 3);
                juego.setImagenUrl3(urlImagen3);
            }
            
            if (imagen4 != null && !imagen4.isEmpty()) {
                String urlImagen4 = servicioJuego.guardarImagenJuego(imagen4, 4);
                juego.setImagenUrl4(urlImagen4);
            }
            
            // Actualizar campos permitidos (descripción y video)
            servicioJuego.actualizarInformacionJuego(juego, 
                juegoActualizado.getDescripcion(), 
                juegoActualizado.getVideoYoutubeUrl());
            
            redirectAttributes.addFlashAttribute("success", "Juego actualizado correctamente");
            return "redirect:/proveedor/mis-juegos";
            
        } catch (IOException e) {
            String errorMessage = "Error al actualizar las imágenes. ";
            if (e.getMessage().contains("No space left")) {
                errorMessage += "No hay espacio suficiente en el servidor.";
            } else if (e.getMessage().contains("Access denied") || e.getMessage().contains("Permission")) {
                errorMessage += "No se tienen permisos para guardar archivos.";
            } else if (e.getMessage().contains("File too large")) {
                errorMessage += "Una o más imágenes superan el tamaño máximo de 10MB.";
            } else {
                errorMessage += "Verifica que las imágenes sean válidas (JPG, PNG, WEBP) y no superen 10MB cada una.";
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/proveedor/editar/" + id;
        }
    }
}
