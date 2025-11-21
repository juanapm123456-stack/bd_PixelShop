package com.example.service;

import com.example.model.Usuario;
import java.util.List;

/**
 * Interfaz para el servicio de gestión de usuarios.
 * Define los métodos que se pueden realizar con usuarios.
 */
public interface IServicioUsuario {
    
    /**
     * Busca un usuario por su email.
     * @param email El correo electrónico del usuario
     * @return El usuario encontrado o lanza excepción si no existe
     */
    Usuario buscarUsuarioPorEmail(String email);
    
    /**
     * Obtiene un usuario por su ID.
     * @param id El identificador del usuario
     * @return El usuario encontrado o lanza excepción si no existe
     */
    Usuario obtenerUsuarioPorId(Long id);
    
    /**
     * Registra un nuevo usuario en el sistema.
     * @param usuario El usuario a registrar
     * @return El usuario registrado
     */
    Usuario registrarNuevoUsuario(Usuario usuario);
    
    /**
     * Verifica si un email ya está registrado en el sistema.
     * @param email El email a verificar
     * @return true si existe, false si no existe
     */
    boolean verificarEmailExistente(String email);
    
    /**
     * Encripta una contraseña usando BCrypt.
     * @param contrasenaPlana La contraseña en texto plano
     * @return La contraseña encriptada
     */
    String encriptarContrasena(String contrasenaPlana);
    
    /**
     * Guarda un usuario en la base de datos.
     * @param usuario El usuario a guardar
     * @return El usuario guardado con su ID generado
     */
    Usuario guardarUsuario(Usuario usuario);
    
    /**
     * Obtiene la lista completa de usuarios registrados.
     * @return Lista de todos los usuarios
     */
    List<Usuario> listarTodosLosUsuarios();
    
    /**
     * Elimina un usuario del sistema por su ID.
     * @param id El ID del usuario a eliminar
     */
    void eliminarUsuario(Long id);
    
    /**
     * Verifica si un usuario tiene rol de administrador.
     * @param usuario El usuario a verificar
     * @return true si es administrador, false si no lo es
     */
    boolean esAdministrador(Usuario usuario);
    
    /**
     * Verifica si un usuario tiene rol de proveedor.
     * @param usuario El usuario a verificar
     * @return true si es proveedor, false si no lo es
     */
    boolean esProveedor(Usuario usuario);
    
    /**
     * Verifica si un usuario tiene rol de cliente.
     * @param usuario El usuario a verificar
     * @return true si es cliente, false si no lo es
     */
    boolean esCliente(Usuario usuario);
    
    /**
     * Activa o desactiva un usuario en el sistema.
     * @param usuario El usuario a modificar
     * @param activo Estado de activación
     * @return El usuario actualizado
     */
    Usuario cambiarEstadoActivacion(Usuario usuario, boolean activo);
}
