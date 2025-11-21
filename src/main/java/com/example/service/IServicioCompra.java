package com.example.service;

import com.example.model.Compra;
import com.example.model.Juego;
import com.example.model.Usuario;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz para el servicio de gestión de compras.
 * Define los métodos para compras y cálculo de comisiones.
 */
public interface IServicioCompra {
    
    /**
     * Crea una nueva compra de un juego.
     * @param usuario El usuario que compra
     * @param juego El juego a comprar
     * @return La compra creada y guardada
     */
    Compra crearNuevaCompra(Usuario usuario, Juego juego);
    
    /**
     * Guarda una compra en la base de datos.
     * @param compra La compra a guardar
     * @return La compra guardada
     */
    Compra guardarCompra(Compra compra);
    
    /**
     * Verifica si un usuario ya compró un juego específico.
     * @param usuario El usuario a verificar
     * @param juego El juego a verificar
     * @return true si ya lo compró, false si no lo compró
     */
    boolean verificarJuegoYaComprado(Usuario usuario, Juego juego);
    
    /**
     * Obtiene todas las compras de un usuario ordenadas por fecha (más recientes primero).
     * @param usuario El usuario del que se quieren obtener las compras
     * @return Lista de compras del usuario
     */
    List<Compra> obtenerComprasDeUsuario(Usuario usuario);
    
    /**
     * Obtiene la lista de IDs de juegos que un usuario ya ha comprado.
     * Útil para mostrar en el catálogo qué juegos ya tiene.
     * @param usuario El usuario del que se quieren obtener los IDs
     * @return Lista de IDs de juegos comprados
     */
    List<Long> obtenerIdsDeJuegosComprados(Usuario usuario);
    
    /**
     * Calcula cuánto dinero debe recibir el proveedor por una venta.
     * El proveedor recibe el 85% del precio de venta.
     * @param precioJuego El precio del juego vendido
     * @return Monto que recibe el proveedor (85% del precio)
     */
    BigDecimal calcularMontoParaProveedor(BigDecimal precioJuego);
    
    /**
     * Calcula cuánto dinero se queda la plataforma por una venta.
     * La plataforma recibe el 15% del precio de venta.
     * @param precioJuego El precio del juego vendido
     * @return Monto que recibe la plataforma (15% del precio)
     */
    BigDecimal calcularComisionPlataforma(BigDecimal precioJuego);
    
    /**
     * Calcula el total de ventas realizadas en la plataforma.
     * @return Suma total de todas las compras realizadas
     */
    BigDecimal calcularTotalVentas();
    
    /**
     * Obtiene todas las compras realizadas en la plataforma.
     * @return Lista completa de compras
     */
    List<Compra> obtenerTodasLasCompras();
    
    /**
     * Filtra y obtiene solo las compras válidas (excluyendo las de administradores).
     * Los administradores no pueden comprar, por lo que sus compras no deben contarse.
     * @return Lista de compras válidas (sin compras de ADMIN)
     */
    List<Compra> obtenerComprasValidas();
    
    /**
     * Valida si un usuario puede realizar compras.
     * Los administradores NO pueden comprar juegos.
     * @param usuario El usuario a validar
     * @return true si puede comprar, false si no puede
     */
    boolean puedeRealizarCompras(Usuario usuario);
    
    /**
     * Marca una compra como pagada al proveedor.
     * @param compra La compra a marcar como pagada
     * @return La compra actualizada
     */
    Compra marcarComoPagadaAlProveedor(Compra compra);
}
