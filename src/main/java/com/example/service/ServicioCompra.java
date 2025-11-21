package com.example.service;

import com.example.model.Compra;
import com.example.model.Juego;
import com.example.model.Usuario;
import com.example.model.Rol;
import com.example.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de compras.
 * Contiene cálculos de comisiones y validaciones de compra.
 */
@Service
public class ServicioCompra implements IServicioCompra {
    
    @Autowired
    private CompraRepository compraRepository;
    
    /**
     * Comisión que se queda la plataforma por cada venta (15%).
     * El 85% restante va para el proveedor.
     */
    private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.15");
    private static final BigDecimal COMISION_PROVEEDOR = new BigDecimal("0.85");
    
    /**
     * Crea una nueva compra de un juego.
     * @param usuario El usuario que compra
     * @param juego El juego a comprar
     * @return La compra creada y guardada
     */
    public Compra crearNuevaCompra(Usuario usuario, Juego juego) {
        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setJuego(juego);
        compra.setPrecioPagado(juego.getPrecio());
        compra.setFechaCompra(LocalDateTime.now());
        compra.setPagadoAlProveedor(false);
        return guardarCompra(compra);
    }
    
    /**
     * Guarda una compra en la base de datos.
     * @param compra La compra a guardar
     * @return La compra guardada
     */
    public Compra guardarCompra(Compra compra) {
        return compraRepository.save(compra);
    }
    
    /**
     * Verifica si un usuario ya compró un juego específico.
     * @param usuario El usuario a verificar
     * @param juego El juego a verificar
     * @return true si ya lo compró, false si no lo compró
     */
    public boolean verificarJuegoYaComprado(Usuario usuario, Juego juego) {
        return compraRepository.existsByUsuarioAndJuego(usuario, juego);
    }
    
    /**
     * Obtiene todas las compras de un usuario ordenadas por fecha (más recientes primero).
     * @param usuario El usuario del que se quieren obtener las compras
     * @return Lista de compras del usuario
     */
    public List<Compra> obtenerComprasDeUsuario(Usuario usuario) {
        return compraRepository.findByUsuarioOrderByFechaCompraDesc(usuario);
    }
    
    /**
     * Obtiene la lista de IDs de juegos que un usuario ya ha comprado.
     * Útil para mostrar en el catálogo qué juegos ya tiene.
     * @param usuario El usuario del que se quieren obtener los IDs
     * @return Lista de IDs de juegos comprados
     */
    public List<Long> obtenerIdsDeJuegosComprados(Usuario usuario) {
        return obtenerComprasDeUsuario(usuario).stream()
            .map(compra -> compra.getJuego().getId())
            .collect(Collectors.toList());
    }
    
    /**
     * Calcula cuánto dinero debe recibir el proveedor por una venta.
     * El proveedor recibe el 85% del precio de venta.
     * @param precioJuego El precio del juego vendido
     * @return Monto que recibe el proveedor (85% del precio)
     */
    public BigDecimal calcularMontoParaProveedor(BigDecimal precioJuego) {
        return precioJuego.multiply(COMISION_PROVEEDOR);
    }
    
    /**
     * Calcula cuánto dinero se queda la plataforma por una venta.
     * La plataforma recibe el 15% del precio de venta.
     * @param precioJuego El precio del juego vendido
     * @return Monto que recibe la plataforma (15% del precio)
     */
    public BigDecimal calcularComisionPlataforma(BigDecimal precioJuego) {
        return precioJuego.multiply(COMISION_PLATAFORMA);
    }
    
    /**
     * Calcula el total de ventas realizadas en la plataforma.
     * @return Suma total de todas las compras realizadas
     */
    public BigDecimal calcularTotalVentas() {
        BigDecimal total = compraRepository.calcularTotalVentas();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Obtiene todas las compras realizadas en la plataforma.
     * @return Lista completa de compras
     */
    public List<Compra> obtenerTodasLasCompras() {
        return compraRepository.findAll();
    }
    
    /**
     * Filtra y obtiene solo las compras válidas (excluyendo las de administradores).
     * Los administradores no pueden comprar, por lo que sus compras no deben contarse.
     * @return Lista de compras válidas (sin compras de ADMIN)
     */
    public List<Compra> obtenerComprasValidas() {
        return obtenerTodasLasCompras().stream()
            .filter(compra -> compra.getUsuario().getRol() != Rol.ADMIN)
            .collect(Collectors.toList());
    }
    
    /**
     * Valida si un usuario puede realizar compras.
     * Los administradores NO pueden comprar juegos.
     * @param usuario El usuario a validar
     * @return true si puede comprar, false si no puede
     */
    public boolean puedeRealizarCompras(Usuario usuario) {
        return usuario.getRol() != Rol.ADMIN;
    }
    
    /**
     * Marca una compra como pagada al proveedor.
     * @param compra La compra a marcar como pagada
     * @return La compra actualizada
     */
    public Compra marcarComoPagadaAlProveedor(Compra compra) {
        compra.setPagadoAlProveedor(true);
        return guardarCompra(compra);
    }
}
