package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * Implementación del servicio de administración.
 * Contiene cálculos de ganancias de la plataforma por ventas y publicaciones.
 */
@Service
public class ServicioAdministrador implements IServicioAdministrador {
    
    @Autowired
    private ServicioCompra servicioCompra;
    
    @Autowired
    private ServicioProveedor servicioProveedor;
    
    /**
     * Calcula las ganancias totales que ha obtenido la plataforma.
     * Las ganancias provienen de dos fuentes:
     * 1. Comisión del 15% por cada venta de juego
     * 2. Cargo de 25€ por cada publicación de juego
     * 
     * @return ResumenGanancias con todas las ganancias desglosadas
     */
    public ResumenGanancias calcularGananciasTotalesPlataforma() {
        // 1. Calcular ganancias por ventas (15% de cada venta)
        BigDecimal totalVentas = servicioCompra.calcularTotalVentas();
        BigDecimal gananciasPorVentas = totalVentas.multiply(new BigDecimal("0.15"));
        
        // 2. Calcular ganancias por publicaciones (25€ por cada publicación)
        Long totalPublicaciones = servicioProveedor.contarPublicacionesPagadas();
        BigDecimal gananciasPorPublicaciones = servicioProveedor
            .calcularIngresosPlataformaPorPublicaciones(totalPublicaciones);
        
        // 3. Calcular ganancia total
        BigDecimal gananciaTotal = gananciasPorVentas.add(gananciasPorPublicaciones);
        
        return new ResumenGanancias(
            gananciasPorVentas,
            gananciasPorPublicaciones,
            gananciaTotal,
            totalVentas,
            totalPublicaciones
        );
    }
    
    /**
     * Calcula cuánto dinero ha generado la plataforma por comisiones de ventas.
     * La plataforma se queda con el 15% de cada venta.
     * 
     * @return Ganancias por ventas (15% del total vendido)
     */
    public BigDecimal calcularGananciasPorVentas() {
        BigDecimal totalVentas = servicioCompra.calcularTotalVentas();
        return servicioCompra.calcularComisionPlataforma(totalVentas);
    }
    
    /**
     * Calcula cuánto dinero ha generado la plataforma por publicaciones de juegos.
     * Cada publicación cuesta 25€.
     * 
     * @return Ganancias por publicaciones (25€ × número de publicaciones)
     */
    public BigDecimal calcularGananciasPorPublicaciones() {
        Long totalPublicaciones = servicioProveedor.contarPublicacionesPagadas();
        return servicioProveedor.calcularIngresosPlataformaPorPublicaciones(totalPublicaciones);
    }
    
    /**
     * Clase interna para encapsular el resumen de ganancias.
     * Facilita el retorno de múltiples valores relacionados.
     */
    public static class ResumenGanancias {
        private final BigDecimal gananciasPorVentas;
        private final BigDecimal gananciasPorPublicaciones;
        private final BigDecimal gananciaTotal;
        private final BigDecimal totalVentas;
        private final Long totalPublicaciones;
        
        public ResumenGanancias(BigDecimal gananciasPorVentas, 
                               BigDecimal gananciasPorPublicaciones,
                               BigDecimal gananciaTotal,
                               BigDecimal totalVentas,
                               Long totalPublicaciones) {
            this.gananciasPorVentas = gananciasPorVentas;
            this.gananciasPorPublicaciones = gananciasPorPublicaciones;
            this.gananciaTotal = gananciaTotal;
            this.totalVentas = totalVentas;
            this.totalPublicaciones = totalPublicaciones;
        }
        
        public BigDecimal getGananciasPorVentas() {
            return gananciasPorVentas;
        }
        
        public BigDecimal getGananciasPorPublicaciones() {
            return gananciasPorPublicaciones;
        }
        
        public BigDecimal getGananciaTotal() {
            return gananciaTotal;
        }
        
        public BigDecimal getTotalVentas() {
            return totalVentas;
        }
        
        public Long getTotalPublicaciones() {
            return totalPublicaciones;
        }
    }
}
