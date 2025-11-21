package com.example.service;

import com.example.model.MovimientoProveedor;
import com.example.model.PublicacionJuego;
import com.example.model.Compra;
import com.example.model.Usuario;
import com.example.model.Juego;
import com.example.repository.MovimientoProveedorRepository;
import com.example.repository.PublicacionJuegoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de proveedores.
 * Contiene movimientos financieros y publicaciones de juegos.
 */
@Service
public class ServicioProveedor implements IServicioProveedor {
    
    @Autowired
    private MovimientoProveedorRepository movimientoRepository;
    
    @Autowired
    private PublicacionJuegoRepository publicacionRepository;
    
    /**
     * Costo que cobra la plataforma por publicar un juego (25€).
     */
    private static final BigDecimal COSTO_PUBLICACION = new BigDecimal("25.00");
    
    /**
     * Crea un movimiento financiero para un proveedor cuando se vende su juego.
     * @param proveedor El proveedor que recibe el dinero
     * @param compra La compra asociada
     * @param montoNeto El monto neto que recibe el proveedor (85% del precio)
     * @return El movimiento creado y guardado
     */
    public MovimientoProveedor crearMovimientoProveedor(Usuario proveedor, Compra compra, BigDecimal montoNeto) {
        MovimientoProveedor movimiento = new MovimientoProveedor();
        movimiento.setProveedor(proveedor);
        movimiento.setCompra(compra);
        movimiento.setMontoNeto(montoNeto);
        movimiento.setPagado(false);
        movimiento.setFecha(LocalDateTime.now());
        return guardarMovimiento(movimiento);
    }
    
    /**
     * Guarda un movimiento de proveedor en la base de datos.
     * @param movimiento El movimiento a guardar
     * @return El movimiento guardado
     */
    public MovimientoProveedor guardarMovimiento(MovimientoProveedor movimiento) {
        return movimientoRepository.save(movimiento);
    }
    
    /**
     * Obtiene todos los movimientos financieros de un proveedor ordenados por fecha.
     * @param proveedor El proveedor del que se quieren obtener los movimientos
     * @return Lista de movimientos del proveedor
     */
    public List<MovimientoProveedor> obtenerMovimientosDeProveedor(Usuario proveedor) {
        return movimientoRepository.findByProveedorOrderByFechaDesc(proveedor);
    }
    
    /**
     * Calcula el total de ingresos pendientes de cobro para un proveedor.
     * Suma todos los movimientos que aún no han sido marcados como pagados.
     * @param proveedor El proveedor del que se quieren calcular los ingresos
     * @return Total de dinero pendiente de cobro
     */
    public BigDecimal calcularIngresosPendientes(Usuario proveedor) {
        BigDecimal ingresos = movimientoRepository.calcularIngresosPendientes(proveedor);
        return ingresos != null ? ingresos : BigDecimal.ZERO;
    }
    
    /**
     * Marca un movimiento como cobrado por el proveedor.
     * @param movimientoId El ID del movimiento a marcar como cobrado
     * @return El movimiento actualizado
     */
    public MovimientoProveedor marcarMovimientoComoCobrado(Long movimientoId) {
        MovimientoProveedor movimiento = buscarMovimientoPorId(movimientoId);
        movimiento.setPagado(true);
        return guardarMovimiento(movimiento);
    }
    
    /**
     * Busca un movimiento por su ID.
     * @param id El ID del movimiento
     * @return El movimiento encontrado o lanza excepción si no existe
     */
    public MovimientoProveedor buscarMovimientoPorId(Long id) {
        return movimientoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Movimiento no encontrado con ID: " + id));
    }
    
    /**
     * Registra la publicación de un juego en la plataforma.
     * Esto genera un cargo de 25€ al proveedor.
     * @param juego El juego publicado
     * @param proveedor El proveedor que publica
     * @return La publicación registrada y guardada
     */
    public PublicacionJuego registrarPublicacionJuego(Juego juego, Usuario proveedor) {
        PublicacionJuego publicacion = new PublicacionJuego();
        publicacion.setJuego(juego);
        publicacion.setProveedor(proveedor);
        publicacion.setFechaPublicacion(LocalDateTime.now());
        publicacion.setPagado(true); // Se asume que se paga al momento de publicar
        return guardarPublicacion(publicacion);
    }
    
    /**
     * Guarda una publicación de juego en la base de datos.
     * @param publicacion La publicación a guardar
     * @return La publicación guardada
     */
    public PublicacionJuego guardarPublicacion(PublicacionJuego publicacion) {
        return publicacionRepository.save(publicacion);
    }
    
    /**
     * Cuenta cuántas publicaciones pagadas hay en total en la plataforma.
     * @return Número total de publicaciones pagadas
     */
    public Long contarPublicacionesPagadas() {
        Long total = publicacionRepository.contarPublicacionesPagadas();
        return total != null ? total : 0L;
    }
    
    /**
     * Obtiene el costo de publicación de un juego en la plataforma.
     * @return El costo de publicar un juego (25€)
     */
    public BigDecimal obtenerCostoPublicacion() {
        return COSTO_PUBLICACION;
    }
    
    /**
     * Calcula el total de ingresos que la plataforma ha recibido por publicaciones.
     * @param numeroPublicaciones Número total de publicaciones pagadas
     * @return Total de ingresos por publicaciones (25€ × número de publicaciones)
     */
    public BigDecimal calcularIngresosPlataformaPorPublicaciones(Long numeroPublicaciones) {
        return COSTO_PUBLICACION.multiply(new BigDecimal(numeroPublicaciones));
    }
}
