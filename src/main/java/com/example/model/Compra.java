package com.example.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compras",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "juego_id"}))
public class Compra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;
    
    @Column(name = "precio_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPagado;
    
    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra;
    
    @Column(name = "pagado_al_proveedor", nullable = false)
    private Boolean pagadoAlProveedor = false;
    
    @OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
    private MovimientoProveedor movimiento;
    
    // Constructores
    public Compra() {}
    
    public Compra(Usuario usuario, Juego juego, BigDecimal precioPagado) {
        this.usuario = usuario;
        this.juego = juego;
        this.precioPagado = precioPagado;
        this.fechaCompra = LocalDateTime.now();
        this.pagadoAlProveedor = false;
    }
    
    // Método para establecer fecha de compra automáticamente
    @PrePersist
    protected void onCreate() {
        if (fechaCompra == null) {
            fechaCompra = LocalDateTime.now();
        }
        if (pagadoAlProveedor == null) {
            pagadoAlProveedor = false;
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Juego getJuego() {
        return juego;
    }
    
    public void setJuego(Juego juego) {
        this.juego = juego;
    }
    
    public BigDecimal getPrecioPagado() {
        return precioPagado;
    }
    
    public void setPrecioPagado(BigDecimal precioPagado) {
        this.precioPagado = precioPagado;
    }
    
    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }
    
    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
    
    public Boolean getPagadoAlProveedor() {
        return pagadoAlProveedor;
    }
    
    public void setPagadoAlProveedor(Boolean pagadoAlProveedor) {
        this.pagadoAlProveedor = pagadoAlProveedor;
    }
    
    public MovimientoProveedor getMovimiento() {
        return movimiento;
    }
    
    public void setMovimiento(MovimientoProveedor movimiento) {
        this.movimiento = movimiento;
    }
}
