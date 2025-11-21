package com.example.service;

import com.example.model.Juego;
import com.example.model.Usuario;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz para el servicio de gestión de juegos.
 * Define los métodos para validación, búsqueda y gestión del catálogo.
 */
public interface IServicioJuego {
    
    /**
     * Busca un juego por su ID.
     * @param id El identificador del juego
     * @return El juego encontrado o lanza excepción si no existe
     */
    Juego buscarJuegoPorId(Long id);
    
    /**
     * Obtiene todos los juegos activos en el catálogo.
     * @return Lista de juegos activos
     */
    List<Juego> obtenerJuegosActivos();
    
    /**
     * Obtiene todos los juegos publicados por un proveedor específico.
     * @param proveedor El proveedor dueño de los juegos
     * @return Lista de juegos del proveedor
     */
    List<Juego> obtenerJuegosDeProveedor(Usuario proveedor);
    
    /**
     * Busca juegos por título (búsqueda parcial, sin distinción de mayúsculas).
     * @param titulo El texto a buscar en el título
     * @return Lista de juegos que coinciden con la búsqueda
     */
    List<Juego> buscarJuegosPorTitulo(String titulo);
    
    /**
     * Guarda un nuevo juego en la base de datos.
     * @param juego El juego a guardar
     * @return El juego guardado con su ID generado
     */
    Juego guardarJuego(Juego juego);
    
    /**
     * Crea un nuevo juego con toda su información básica.
     * @param titulo Título del juego
     * @param descripcion Descripción del juego
     * @param precio Precio del juego
     * @param genero Género del juego
     * @param proveedor Proveedor que publica el juego
     * @return El juego creado (sin guardar)
     */
    Juego crearNuevoJuego(String titulo, String descripcion, BigDecimal precio, 
                          String genero, Usuario proveedor);
    
    /**
     * Valida que una imagen cumpla con los requisitos de tamaño y dimensiones.
     * @param archivo Archivo de imagen a validar
     * @param nombreImagen Nombre descriptivo de la imagen (para mensajes de error)
     * @return null si es válida, mensaje de error si no cumple requisitos
     */
    String validarImagen(MultipartFile archivo, String nombreImagen);
    
    /**
     * Guarda una imagen en el servidor y retorna su URL.
     * @param archivo Archivo de imagen a guardar
     * @param numeroImagen Número de imagen (1, 2, 3 o 4)
     * @return URL relativa de la imagen guardada
     * @throws IOException Si hay error al guardar el archivo
     */
    String guardarImagenJuego(MultipartFile archivo, int numeroImagen) throws IOException;
    
    /**
     * Actualiza los campos permitidos de un juego existente.
     * Solo se puede actualizar descripción y video de YouTube.
     * @param juego El juego a actualizar
     * @param nuevaDescripcion Nueva descripción
     * @param nuevoVideoUrl Nueva URL de YouTube
     * @return El juego actualizado
     */
    Juego actualizarInformacionJuego(Juego juego, String nuevaDescripcion, String nuevoVideoUrl);
    
    /**
     * Verifica si un usuario es el propietario de un juego.
     * @param juego El juego a verificar
     * @param usuario El usuario a verificar
     * @return true si el usuario es el propietario, false si no lo es
     */
    boolean esProveedorDelJuego(Juego juego, Usuario usuario);
    
    /**
     * Activa o desactiva un juego en el catálogo.
     * @param juego El juego a modificar
     * @param activo Estado de activación
     * @return El juego actualizado
     */
    Juego cambiarEstadoActivacion(Juego juego, boolean activo);
}
