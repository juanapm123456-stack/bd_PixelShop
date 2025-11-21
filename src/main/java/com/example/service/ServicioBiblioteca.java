package com.example.service;

import com.example.model.BibliotecaUsuario;
import com.example.model.Usuario;
import com.example.model.Juego;
import com.example.repository.BibliotecaUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de biblioteca.
 * Contiene la gestión de la biblioteca de juegos de usuarios.
 */
@Service
public class ServicioBiblioteca implements IServicioBiblioteca {
    
    @Autowired
    private BibliotecaUsuarioRepository bibliotecaRepository;
    
    /**
     * Agrega un juego a la biblioteca de un usuario.
     * Esto se hace automáticamente cuando se realiza una compra.
     * @param usuario El usuario que adquiere el juego
     * @param juego El juego adquirido
     * @return El registro de biblioteca creado y guardado
     */
    public BibliotecaUsuario agregarJuegoABiblioteca(Usuario usuario, Juego juego) {
        BibliotecaUsuario biblioteca = new BibliotecaUsuario();
        biblioteca.setUsuario(usuario);
        biblioteca.setJuego(juego);
        biblioteca.setFechaAdquisicion(LocalDateTime.now());
        return guardarRegistroBiblioteca(biblioteca);
    }
    
    /**
     * Guarda un registro de biblioteca en la base de datos.
     * @param biblioteca El registro a guardar
     * @return El registro guardado
     */
    public BibliotecaUsuario guardarRegistroBiblioteca(BibliotecaUsuario biblioteca) {
        return bibliotecaRepository.save(biblioteca);
    }
    
    /**
     * Obtiene todos los juegos en la biblioteca de un usuario.
     * Los juegos se ordenan por fecha de adquisición (más recientes primero).
     * @param usuario El usuario del que se quiere obtener la biblioteca
     * @return Lista de registros de biblioteca del usuario
     */
    public List<BibliotecaUsuario> obtenerBibliotecaDeUsuario(Usuario usuario) {
        return bibliotecaRepository.findByUsuarioOrderByFechaAdquisicionDesc(usuario);
    }
    
    /**
     * Cuenta cuántos juegos tiene un usuario en su biblioteca.
     * @param usuario El usuario del que se quiere contar los juegos
     * @return Número de juegos en la biblioteca
     */
    public int contarJuegosEnBiblioteca(Usuario usuario) {
        return obtenerBibliotecaDeUsuario(usuario).size();
    }
    
    /**
     * Verifica si un usuario tiene un juego específico en su biblioteca.
     * @param usuario El usuario a verificar
     * @param juego El juego a buscar
     * @return true si el usuario tiene el juego, false si no lo tiene
     */
    public boolean usuarioTieneJuegoEnBiblioteca(Usuario usuario, Juego juego) {
        return bibliotecaRepository.existsByUsuarioAndJuego(usuario, juego);
    }
}
