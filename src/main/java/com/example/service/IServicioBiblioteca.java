package com.example.service;

import com.example.model.BibliotecaUsuario;
import com.example.model.Usuario;
import com.example.model.Juego;
import java.util.List;

/**
 * Interfaz para el servicio de gestión de biblioteca.
 * Define los métodos para gestionar la biblioteca de juegos de usuarios.
 */
public interface IServicioBiblioteca {
    
    /**
     * Agrega un juego a la biblioteca de un usuario.
     * Esto se hace automáticamente cuando se realiza una compra.
     * @param usuario El usuario que adquiere el juego
     * @param juego El juego adquirido
     * @return El registro de biblioteca creado y guardado
     */
    BibliotecaUsuario agregarJuegoABiblioteca(Usuario usuario, Juego juego);
    
    /**
     * Guarda un registro de biblioteca en la base de datos.
     * @param biblioteca El registro a guardar
     * @return El registro guardado
     */
    BibliotecaUsuario guardarRegistroBiblioteca(BibliotecaUsuario biblioteca);
    
    /**
     * Obtiene todos los juegos en la biblioteca de un usuario.
     * Los juegos se ordenan por fecha de adquisición (más recientes primero).
     * @param usuario El usuario del que se quiere obtener la biblioteca
     * @return Lista de registros de biblioteca del usuario
     */
    List<BibliotecaUsuario> obtenerBibliotecaDeUsuario(Usuario usuario);
    
    /**
     * Cuenta cuántos juegos tiene un usuario en su biblioteca.
     * @param usuario El usuario del que se quiere contar los juegos
     * @return Número de juegos en la biblioteca
     */
    int contarJuegosEnBiblioteca(Usuario usuario);
    
    /**
     * Verifica si un usuario tiene un juego específico en su biblioteca.
     * @param usuario El usuario a verificar
     * @param juego El juego a buscar
     * @return true si el usuario tiene el juego, false si no lo tiene
     */
    boolean usuarioTieneJuegoEnBiblioteca(Usuario usuario, Juego juego);
}
