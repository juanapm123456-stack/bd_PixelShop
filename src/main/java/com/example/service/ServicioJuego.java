package com.example.service;

import com.example.model.Juego;
import com.example.model.Usuario;
import com.example.repository.JuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;

/**
 * Implementación del servicio de gestión de juegos.
 * Contiene validación de imágenes, búsqueda y gestión del catálogo.
 */
@Service
public class ServicioJuego implements IServicioJuego {
    
    @Autowired
    private JuegoRepository juegoRepository;
    
    /**
     * Busca un juego por su ID.
     * @param id El identificador del juego
     * @return El juego encontrado o lanza excepción si no existe
     */
    public Juego buscarJuegoPorId(Long id) {
        return juegoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Juego no encontrado con ID: " + id));
    }
    
    /**
     * Obtiene todos los juegos activos en el catálogo.
     * @return Lista de juegos activos
     */
    public List<Juego> obtenerJuegosActivos() {
        return juegoRepository.findByActivoTrue();
    }
    
    /**
     * Obtiene todos los juegos publicados por un proveedor específico.
     * @param proveedor El proveedor dueño de los juegos
     * @return Lista de juegos del proveedor
     */
    public List<Juego> obtenerJuegosDeProveedor(Usuario proveedor) {
        return juegoRepository.findByProveedor(proveedor);
    }
    
    /**
     * Busca juegos por título (búsqueda parcial, sin distinción de mayúsculas).
     * @param titulo El texto a buscar en el título
     * @return Lista de juegos que coinciden con la búsqueda
     */
    public List<Juego> buscarJuegosPorTitulo(String titulo) {
        return juegoRepository.findByTituloContainingIgnoreCaseAndActivoTrue(titulo);
    }
    
    /**
     * Guarda un nuevo juego en la base de datos.
     * @param juego El juego a guardar
     * @return El juego guardado con su ID generado
     */
    public Juego guardarJuego(Juego juego) {
        return juegoRepository.save(juego);
    }
    
    /**
     * Crea un nuevo juego con toda su información básica.
     * @param titulo Título del juego
     * @param descripcion Descripción del juego
     * @param precio Precio del juego
     * @param genero Género del juego
     * @param proveedor Proveedor que publica el juego
     * @return El juego creado (sin guardar)
     */
    public Juego crearNuevoJuego(String titulo, String descripcion, BigDecimal precio, 
                                  String genero, Usuario proveedor) {
        Juego juego = new Juego();
        juego.setTitulo(titulo);
        juego.setDescripcion(descripcion);
        juego.setPrecio(precio);
        juego.setGenero(genero);
        juego.setProveedor(proveedor);
        juego.setFechaPublicacion(LocalDateTime.now());
        juego.setActivo(true);
        return juego;
    }
    
    /**
     * Valida que una imagen cumpla con los requisitos de tamaño y dimensiones.
     * @param archivo Archivo de imagen a validar
     * @param nombreImagen Nombre descriptivo de la imagen (para mensajes de error)
     * @return null si es válida, mensaje de error si no cumple requisitos
     */
    public String validarImagen(MultipartFile archivo, String nombreImagen) {
        try {
            // Validar tamaño máximo de 10MB
            long tamanoMaximo = 10 * 1024 * 1024; // 10MB en bytes
            if (archivo.getSize() > tamanoMaximo) {
                return String.format("%s es demasiado grande. Máximo: 10MB. Tu archivo: %.2f MB", 
                    nombreImagen, archivo.getSize() / (1024.0 * 1024.0));
            }
            
            // Leer y validar dimensiones
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(archivo.getBytes()));
            if (imagen == null) {
                return nombreImagen + " no es un archivo de imagen válido";
            }
            
            int ancho = imagen.getWidth();
            int alto = imagen.getHeight();
            
            // Validar dimensiones mínimas
            if (ancho < 200 || alto < 200) {
                return String.format("%s es muy pequeña. Mínimo recomendado: 200x200px. Tu imagen: %dx%dpx (%.0f KB)", 
                    nombreImagen, ancho, alto, archivo.getSize() / 1024.0);
            }
            
            // Validar dimensiones máximas
            if (ancho > 3000 || alto > 3000) {
                return String.format("%s es muy grande. Máximo recomendado: 3000x3000px. Tu imagen: %dx%dpx (%.0f KB)", 
                    nombreImagen, ancho, alto, archivo.getSize() / 1024.0);
            }
            
            return null; // Imagen válida
            
        } catch (IOException e) {
            return "Error al procesar " + nombreImagen + ". Verifica que sea un archivo de imagen válido (JPG, PNG, WEBP).";
        }
    }
    
    /**
     * Guarda una imagen en el servidor y retorna su URL.
     * @param archivo Archivo de imagen a guardar
     * @param numeroImagen Número de imagen (1, 2, 3 o 4)
     * @return URL relativa de la imagen guardada
     * @throws IOException Si hay error al guardar el archivo
     */
    public String guardarImagenJuego(MultipartFile archivo, int numeroImagen) throws IOException {
        // Obtener directorio de imágenes
        String rutaProyecto = System.getProperty("user.dir");
        String directorioImagenes = rutaProyecto + "/src/main/resources/static/images/juegos/";
        
        // Crear directorio si no existe
        File directorio = new File(directorioImagenes);
        if (!directorio.exists()) {
            boolean creado = directorio.mkdirs();
            if (!creado) {
                throw new IOException("No se pudo crear el directorio: " + directorioImagenes);
            }
        }
        
        // Validar permisos de escritura
        if (!directorio.canWrite()) {
            throw new IOException("No se tienen permisos de escritura en: " + directorioImagenes);
        }
        
        // Generar nombre único para la imagen
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        String nombreArchivo = System.currentTimeMillis() + "_" + numeroImagen + extension;
        
        // Guardar archivo
        Path rutaArchivo = Paths.get(directorioImagenes + nombreArchivo);
        Files.write(rutaArchivo, archivo.getBytes());
        
        // Retornar URL relativa
        return "/images/juegos/" + nombreArchivo;
    }
    
    /**
     * Actualiza los campos permitidos de un juego existente.
     * Solo se puede actualizar descripción y video de YouTube.
     * @param juego El juego a actualizar
     * @param nuevaDescripcion Nueva descripción
     * @param nuevoVideoUrl Nueva URL de YouTube
     * @return El juego actualizado
     */
    public Juego actualizarInformacionJuego(Juego juego, String nuevaDescripcion, String nuevoVideoUrl) {
        juego.setDescripcion(nuevaDescripcion);
        juego.setVideoYoutubeUrl(nuevoVideoUrl);
        return guardarJuego(juego);
    }
    
    /**
     * Verifica si un usuario es el propietario de un juego.
     * @param juego El juego a verificar
     * @param usuario El usuario a verificar
     * @return true si el usuario es el propietario, false si no lo es
     */
    public boolean esProveedorDelJuego(Juego juego, Usuario usuario) {
        return juego.getProveedor().getId().equals(usuario.getId());
    }
    
    /**
     * Activa o desactiva un juego en el catálogo.
     * @param juego El juego a modificar
     * @param activo Estado de activación
     * @return El juego actualizado
     */
    public Juego cambiarEstadoActivacion(Juego juego, boolean activo) {
        juego.setActivo(activo);
        return guardarJuego(juego);
    }
}
