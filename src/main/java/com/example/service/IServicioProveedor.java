package com.example.service;

import com.example.model.MovimientoProveedor;
import com.example.model.PublicacionJuego;
import com.example.model.Compra;
import com.example.model.Usuario;
import com.example.model.Juego;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interfaz para el servicio de gestión de proveedores.
 * Define los métodos para movimientos financieros y publicaciones.
 */
public interface IServicioProveedor {
    
    /**
     * Crea un movimiento financiero para un proveedor cuando se vende su juego.
     * @param proveedor El proveedor que recibe el dinero
     * @param compra La compra asociada
     * @param montoNeto El monto neto que recibe el proveedor (85% del precio)
     * @return El movimiento creado y guardado
     */
    MovimientoProveedor crearMovimientoProveedor(Usuario proveedor, Compra compra, BigDecimal montoNeto);
    
    /**
     * Guarda un movimiento de proveedor en la base de datos.
     * @param movimiento El movimiento a guardar
     * @return El movimiento guardado
     */
    MovimientoProveedor guardarMovimiento(MovimientoProveedor movimiento);
    
    /**
     * Obtiene todos los movimientos financieros de un proveedor ordenados por fecha.
     * @param proveedor El proveedor del que se quieren obtener los movimientos
     * @return Lista de movimientos del proveedor
     */
    List<MovimientoProveedor> obtenerMovimientosDeProveedor(Usuario proveedor);
    
    /**
     * Calcula el total de ingresos pendientes de cobro para un proveedor.
     * Suma todos los movimientos que aún no han sido marcados como pagados.
     * @param proveedor El proveedor del que se quieren calcular los ingresos
     * @return Total de dinero pendiente de cobro
     */
    BigDecimal calcularIngresosPendientes(Usuario proveedor);
    
    /**
     * Marca un movimiento como cobrado por el proveedor.
     * @param movimientoId El ID del movimiento a marcar como cobrado
     * @return El movimiento actualizado
     */
    MovimientoProveedor marcarMovimientoComoCobrado(Long movimientoId);
    
    /**
     * Busca un movimiento por su ID.
     * @param id El ID del movimiento
     * @return El movimiento encontrado o lanza excepción si no existe
     */
    MovimientoProveedor buscarMovimientoPorId(Long id);
    
    /**
     * Registra la publicación de un juego en la plataforma.
     * Esto genera un cargo de 25€ al proveedor.
     * @param juego El juego publicado
     * @param proveedor El proveedor que publica
     * @return La publicación registrada y guardada
     */
    PublicacionJuego registrarPublicacionJuego(Juego juego, Usuario proveedor);
    
    /**
     * Guarda una publicación de juego en la base de datos.
     * @param publicacion La publicación a guardar
     * @return La publicación guardada
     */
    PublicacionJuego guardarPublicacion(PublicacionJuego publicacion);
    
    /**
     * Cuenta cuántas publicaciones pagadas hay en total en la plataforma.
     * @return Número total de publicaciones pagadas
     */
    Long contarPublicacionesPagadas();
    
    /**
     * Obtiene el costo de publicación de un juego en la plataforma.
     * @return El costo de publicar un juego (25€)
     */
    BigDecimal obtenerCostoPublicacion();
    
    /**
     * Calcula el total de ingresos que la plataforma ha recibido por publicaciones.
     * @param numeroPublicaciones Número total de publicaciones pagadas
     * @return Total de ingresos por publicaciones (25€ × número de publicaciones)
     */
    BigDecimal calcularIngresosPlataformaPorPublicaciones(Long numeroPublicaciones);
}
