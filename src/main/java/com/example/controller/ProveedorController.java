package com.example.controller;

import com.example.model.*;
import com.example.repository.*;
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
import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

@Controller
@RequestMapping("/proveedor")
@PreAuthorize("hasAnyRole('PROVEEDOR', 'ADMIN')")
public class ProveedorController {
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PublicacionJuegoRepository publicacionRepository;
    
    @Autowired
    private MovimientoProveedorRepository movimientoRepository;
    
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
        
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        try {
            // Directorio para guardar imágenes - usar ruta absoluta
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + "/src/main/resources/static/images/juegos/";
            File directory = new File(uploadDir);
            
            // Crear directorio si no existe
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    redirectAttributes.addFlashAttribute("error", "No se pudo crear el directorio: " + uploadDir + ". Verifica permisos.");
                    return "redirect:/proveedor/publicar";
                }
            }
            
            // Verificar que el directorio sea escribible
            if (!directory.canWrite()) {
                redirectAttributes.addFlashAttribute("error", "No se tienen permisos de escritura en: " + uploadDir);
                return "redirect:/proveedor/publicar";
            }
            
            
            // Guardar imagen 1
            if (!imagen1.isEmpty()) {
                // Validar imagen y obtener dimensiones
                String validationResult = validateImage(imagen1, "Imagen 1");
                if (validationResult != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult);
                    return "redirect:/proveedor/publicar";
                }
                
                String originalName = imagen1.getOriginalFilename();
                String extension = originalName.substring(originalName.lastIndexOf("."));
                String filename1 = System.currentTimeMillis() + "_1" + extension;
                
                Path path1 = Paths.get(uploadDir + filename1);
                Files.write(path1, imagen1.getBytes());
                juego.setImagenUrl1("/images/juegos/" + filename1);
            }
            
            // Guardar imagen 2
            if (!imagen2.isEmpty()) {
                // Validar imagen y obtener dimensiones
                String validationResult2 = validateImage(imagen2, "Imagen 2");
                if (validationResult2 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult2);
                    return "redirect:/proveedor/publicar";
                }
                
                String originalName2 = imagen2.getOriginalFilename();
                String extension2 = originalName2.substring(originalName2.lastIndexOf("."));
                String filename2 = System.currentTimeMillis() + "_2" + extension2;
                
                Path path2 = Paths.get(uploadDir + filename2);
                Files.write(path2, imagen2.getBytes());
                juego.setImagenUrl2("/images/juegos/" + filename2);
            }
            
            // Guardar imagen 3
            if (!imagen3.isEmpty()) {
                // Validar imagen y obtener dimensiones
                String validationResult3 = validateImage(imagen3, "Imagen 3");
                if (validationResult3 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult3);
                    return "redirect:/proveedor/publicar";
                }
                
                String originalName3 = imagen3.getOriginalFilename();
                String extension3 = originalName3.substring(originalName3.lastIndexOf("."));
                String filename3 = System.currentTimeMillis() + "_3" + extension3;
                
                Path path3 = Paths.get(uploadDir + filename3);
                Files.write(path3, imagen3.getBytes());
                juego.setImagenUrl3("/images/juegos/" + filename3);
            }
            
            // Guardar imagen 4
            if (!imagen4.isEmpty()) {
                // Validar imagen y obtener dimensiones
                String validationResult4 = validateImage(imagen4, "Imagen 4");
                if (validationResult4 != null) {
                    redirectAttributes.addFlashAttribute("error", validationResult4);
                    return "redirect:/proveedor/publicar";
                }
                
                String originalName4 = imagen4.getOriginalFilename();
                String extension4 = originalName4.substring(originalName4.lastIndexOf("."));
                String filename4 = System.currentTimeMillis() + "_4" + extension4;
                
                Path path4 = Paths.get(uploadDir + filename4);
                Files.write(path4, imagen4.getBytes());
                juego.setImagenUrl4("/images/juegos/" + filename4);
            }
            
            // Validar que se hayan subido las 4 imágenes
            if (juego.getImagenUrl1() == null || juego.getImagenUrl2() == null || 
                juego.getImagenUrl3() == null || juego.getImagenUrl4() == null) {
                redirectAttributes.addFlashAttribute("error", "Debes subir las 4 imágenes obligatorias (1 portada + 3 capturas)");
                return "redirect:/proveedor/publicar";
            }
            
            // Configurar juego
            juego.setProveedor(proveedor);
            juego.setFechaPublicacion(LocalDateTime.now());
            juego.setActivo(true);
            juegoRepository.save(juego);
            
            // Registrar PUBLICACION (cobro de 25€)
            PublicacionJuego publicacion = new PublicacionJuego();
            publicacion.setJuego(juego);
            publicacion.setProveedor(proveedor);
            publicacion.setFechaPublicacion(LocalDateTime.now());
            publicacion.setPagado(true); // Simular pago de 25€
            publicacionRepository.save(publicacion);
            
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
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Juego> juegos = juegoRepository.findByProveedor(proveedor);
        model.addAttribute("juegos", juegos);
        
        return "proveedor/mis-juegos";
    }
    
    @GetMapping("/ventas")
    public String ventas(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<MovimientoProveedor> movimientos = movimientoRepository.findByProveedorOrderByFechaDesc(proveedor);
        BigDecimal ingresosPendientes = movimientoRepository.calcularIngresosPendientes(proveedor);
        
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("ingresosPendientes", ingresosPendientes != null ? ingresosPendientes : BigDecimal.ZERO);
        
        return "proveedor/ventas";
    }
    
    @PostMapping("/cobrar/{movimientoId}")
    public String cobrarMovimiento(@PathVariable Long movimientoId, RedirectAttributes redirectAttributes) {
        MovimientoProveedor movimiento = movimientoRepository.findById(movimientoId)
            .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
        
        movimiento.setPagado(true);
        movimientoRepository.save(movimiento);
        
        redirectAttributes.addFlashAttribute("success", "Movimiento marcado como cobrado");
        return "redirect:/proveedor/ventas";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarEditar(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // VALIDAR: Solo el proveedor dueño puede editar
        if (!juego.getProveedor().getId().equals(proveedor.getId())) {
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
        
        Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Juego juego = juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado"));
        
        // VALIDAR: Solo el proveedor dueño puede editar
        if (!juego.getProveedor().getId().equals(proveedor.getId())) {
            return "redirect:/proveedor/mis-juegos";
        }
        
        try {
            // Directorio para guardar imágenes - usar ruta absoluta
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + "/src/main/resources/static/images/juegos/";
            File directory = new File(uploadDir);
            
            // Crear directorio si no existe
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    redirectAttributes.addFlashAttribute("error", "No se pudo crear el directorio: " + uploadDir + ". Verifica permisos.");
                    return "redirect:/proveedor/editar/" + id;
                }
            }
            
            // Verificar que el directorio sea escribible
            if (!directory.canWrite()) {
                redirectAttributes.addFlashAttribute("error", "No se tienen permisos de escritura en: " + uploadDir);
                return "redirect:/proveedor/editar/" + id;
            }
            
            // Actualizar imagen 1 (si se sube nueva)
            if (imagen1 != null && !imagen1.isEmpty()) {
                String originalName = imagen1.getOriginalFilename();
                String extension = originalName.substring(originalName.lastIndexOf("."));
                String filename1 = System.currentTimeMillis() + "_1" + extension;
                
                Path path1 = Paths.get(uploadDir + filename1);
                Files.write(path1, imagen1.getBytes());
                juego.setImagenUrl1("/images/juegos/" + filename1);
            }
            
            // Actualizar imagen 2 (si se sube nueva)
            if (imagen2 != null && !imagen2.isEmpty()) {
                String originalName2 = imagen2.getOriginalFilename();
                String extension2 = originalName2.substring(originalName2.lastIndexOf("."));
                String filename2 = System.currentTimeMillis() + "_2" + extension2;
                
                Path path2 = Paths.get(uploadDir + filename2);
                Files.write(path2, imagen2.getBytes());
                juego.setImagenUrl2("/images/juegos/" + filename2);
            }
            
            // Actualizar imagen 3 (si se sube nueva)
            if (imagen3 != null && !imagen3.isEmpty()) {
                String originalName3 = imagen3.getOriginalFilename();
                String extension3 = originalName3.substring(originalName3.lastIndexOf("."));
                String filename3 = System.currentTimeMillis() + "_3" + extension3;
                
                Path path3 = Paths.get(uploadDir + filename3);
                Files.write(path3, imagen3.getBytes());
                juego.setImagenUrl3("/images/juegos/" + filename3);
            }
            
            // Actualizar imagen 4 (si se sube nueva)
            if (imagen4 != null && !imagen4.isEmpty()) {
                String originalName4 = imagen4.getOriginalFilename();
                String extension4 = originalName4.substring(originalName4.lastIndexOf("."));
                String filename4 = System.currentTimeMillis() + "_4" + extension4;
                
                Path path4 = Paths.get(uploadDir + filename4);
                Files.write(path4, imagen4.getBytes());
                juego.setImagenUrl4("/images/juegos/" + filename4);
            }
            
            // Actualizar campos permitidos
            juego.setDescripcion(juegoActualizado.getDescripcion());
            juego.setVideoYoutubeUrl(juegoActualizado.getVideoYoutubeUrl());
            
            // NO ACTUALIZAR: título, precio, proveedor, fecha publicación
            juegoRepository.save(juego);
            
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
    
    /**
     * Valida una imagen y devuelve información sobre sus dimensiones y tamaño
     * @param file Archivo de imagen a validar
     * @param imageName Nombre descriptivo de la imagen
     * @return null si es válida, mensaje de error si no es válida
     */
    private String validateImage(MultipartFile file, String imageName) {
        try {
            // Validar tamaño del archivo (10MB máximo)
            long maxSize = 10 * 1024 * 1024; // 10MB en bytes
            if (file.getSize() > maxSize) {
                return imageName + " es demasiado grande. Máximo: 10MB. Tu archivo: " + 
                       String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0));
            }
            
            // Leer dimensiones de la imagen
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (image == null) {
                return imageName + " no es un archivo de imagen válido";
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            // Información para el usuario (no es error, solo informativo)
            // Recomendaciones: ancho mínimo 300px, máximo 2000px
            if (width < 200 || height < 200) {
                return imageName + " es muy pequeña. Mínimo recomendado: 200x200px. " +
                       "Tu imagen: " + width + "x" + height + "px (" + 
                       String.format("%.0f KB", file.getSize() / 1024.0) + ")";
            }
            
            if (width > 3000 || height > 3000) {
                return imageName + " es muy grande. Máximo recomendado: 3000x3000px. " +
                       "Tu imagen: " + width + "x" + height + "px (" + 
                       String.format("%.0f KB", file.getSize() / 1024.0) + ")";
            }
            
            // Todo OK - imagen válida
            return null;
            
        } catch (IOException e) {
            return "Error al procesar " + imageName + ". Verifica que sea un archivo de imagen válido (JPG, PNG, WEBP).";
        }
    }
}
