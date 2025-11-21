package com.example.service;

import com.example.model.Usuario;
import com.example.model.Rol;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de usuarios.
 * Contiene toda la lógica de negocio relacionada con usuarios.
 */
@Service
public class ServicioUsuario implements IServicioUsuario {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Busca un usuario por su email.
     * @param email El correo electrónico del usuario
     * @return El usuario encontrado o lanza excepción si no existe
     */
    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }
    
    /**
     * Obtiene un usuario por su ID.
     * @param id El identificador del usuario
     * @return El usuario encontrado o lanza excepción si no existe
     */
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     * @param usuario El usuario a registrar
     * @return El usuario registrado
     */
    public Usuario registrarNuevoUsuario(Usuario usuario) {
        // Verificar si el email ya está registrado
        if (verificarEmailExistente(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Encriptar la contraseña para seguridad
        usuario.setPassword(encriptarContrasena(usuario.getPassword()));
        
        // Establecer rol por defecto como CLIENTE
        usuario.setRol(Rol.CLIENTE);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario.setActivo(true);
        
        return guardarUsuario(usuario);
    }
    
    /**
     * Verifica si un email ya está registrado en el sistema.
     * @param email El email a verificar
     * @return true si existe, false si no existe
     */
    public boolean verificarEmailExistente(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    /**
     * Encripta una contraseña usando BCrypt.
     * @param contrasenaPlana La contraseña en texto plano
     * @return La contraseña encriptada
     */
    public String encriptarContrasena(String contrasenaPlana) {
        return passwordEncoder.encode(contrasenaPlana);
    }
    
    /**
     * Guarda un usuario en la base de datos.
     * @param usuario El usuario a guardar
     * @return El usuario guardado con su ID generado
     */
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    /**
     * Obtiene la lista completa de usuarios registrados.
     * @return Lista de todos los usuarios
     */
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
    
    /**
     * Elimina un usuario del sistema por su ID.
     * @param id El ID del usuario a eliminar
     */
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
    
    /**
     * Verifica si un usuario tiene rol de administrador.
     * @param usuario El usuario a verificar
     * @return true si es administrador, false si no lo es
     */
    public boolean esAdministrador(Usuario usuario) {
        return usuario.getRol() == Rol.ADMIN;
    }
    
    /**
     * Verifica si un usuario tiene rol de proveedor.
     * @param usuario El usuario a verificar
     * @return true si es proveedor, false si no lo es
     */
    public boolean esProveedor(Usuario usuario) {
        return usuario.getRol() == Rol.PROVEEDOR;
    }
    
    /**
     * Verifica si un usuario tiene rol de cliente.
     * @param usuario El usuario a verificar
     * @return true si es cliente, false si no lo es
     */
    public boolean esCliente(Usuario usuario) {
        return usuario.getRol() == Rol.CLIENTE;
    }
    
    /**
     * Activa o desactiva un usuario en el sistema.
     * @param usuario El usuario a modificar
     * @param activo Estado de activación
     * @return El usuario actualizado
     */
    public Usuario cambiarEstadoActivacion(Usuario usuario, boolean activo) {
        usuario.setActivo(activo);
        return guardarUsuario(usuario);
    }
}
