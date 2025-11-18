package com.example.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_proveedor")
public class MovimientoProveedor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Usuario proveedor;
    
    @OneToOne
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;
    
    @Column(name = "monto_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoNeto; // 85% del precio
    
    @Column(nullable = false)
    private Boolean pagado = false;
    
    @Column(nullable = false)
    private LocalDateTime fecha;
    
    // Constructores
    public MovimientoProveedor() {}
    
    public MovimientoProveedor(Usuario proveedor, Compra compra, BigDecimal montoNeto) {
        this.proveedor = proveedor;
        this.compra = compra;
        this.montoNeto = montoNeto;
        this.fecha = LocalDateTime.now();
        this.pagado = false;
    }
    
    public MovimientoProveedor(Usuario proveedor, Compra compra, BigDecimal montoNeto, Boolean pagado) {
        this(proveedor, compra, montoNeto);
        this.pagado = pagado;
    }
    
    // Método para establecer fecha automáticamente
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (pagado == null) {
            pagado = false;
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(Usuario proveedor) {
        this.proveedor = proveedor;
    }
    
    public Compra getCompra() {
        return compra;
    }
    
    public void setCompra(Compra compra) {
        this.compra = compra;
    }
    
    public BigDecimal getMontoNeto() {
        return montoNeto;
    }
    
    public void setMontoNeto(BigDecimal montoNeto) {
        this.montoNeto = montoNeto;
    }
    
    public Boolean getPagado() {
        return pagado;
    }
    
    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
