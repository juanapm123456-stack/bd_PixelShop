package com.example.service;

import java.math.BigDecimal;

/**
 * Interfaz para el servicio de administración.
 * Define los métodos para cálculo de ganancias de la plataforma.
 */
public interface IServicioAdministrador {
    
    /**
     * Calcula las ganancias totales que ha obtenido la plataforma.
     * Las ganancias provienen de dos fuentes:
     * 1. Comisión del 15% por cada venta de juego
     * 2. Cargo de 25€ por cada publicación de juego
     * 
     * @return ResumenGanancias con todas las ganancias desglosadas
     */
    ServicioAdministrador.ResumenGanancias calcularGananciasTotalesPlataforma();
    
    /**
     * Calcula cuánto dinero ha generado la plataforma por comisiones de ventas.
     * La plataforma se queda con el 15% de cada venta.
     * 
     * @return Ganancias por ventas (15% del total vendido)
     */
    BigDecimal calcularGananciasPorVentas();
    
    /**
     * Calcula cuánto dinero ha generado la plataforma por publicaciones de juegos.
     * Cada publicación cuesta 25€.
     * 
     * @return Ganancias por publicaciones (25€ × número de publicaciones)
     */
    BigDecimal calcularGananciasPorPublicaciones();
}
