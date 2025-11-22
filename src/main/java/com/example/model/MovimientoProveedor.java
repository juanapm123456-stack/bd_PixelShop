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
    
    @Column(name = "fecha_cobro")
    private LocalDateTime fechaCobro;
    
    @Column(name = "email_paypal_proveedor", length = 255)
    private String emailPayPalProveedor;
    
    @Column(name = "payout_batch_id", length = 100)
    private String payoutBatchId; // ID del lote de pago de PayPal
    
    @Column(name = "metodo_cobro", length = 20)
    private String metodoCobro; // "PAYPAL" o "TARJETA"
    
    @Column(name = "numero_tarjeta", length = 19)
    private String numeroTarjeta; // Últimos 4 dígitos o número enmascarado
    
    @Column(name = "titular_tarjeta", length = 100)
    private String titularTarjeta;
    
    @Column(name = "iban", length = 34)
    private String iban; // IBAN para transferencias
    
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
    
    public LocalDateTime getFechaCobro() {
        return fechaCobro;
    }
    
    public void setFechaCobro(LocalDateTime fechaCobro) {
        this.fechaCobro = fechaCobro;
    }
    
    public String getEmailPayPalProveedor() {
        return emailPayPalProveedor;
    }
    
    public void setEmailPayPalProveedor(String emailPayPalProveedor) {
        this.emailPayPalProveedor = emailPayPalProveedor;
    }
    
    public String getPayoutBatchId() {
        return payoutBatchId;
    }
    
    public void setPayoutBatchId(String payoutBatchId) {
        this.payoutBatchId = payoutBatchId;
    }
    
    public String getMetodoCobro() {
        return metodoCobro;
    }
    
    public void setMetodoCobro(String metodoCobro) {
        this.metodoCobro = metodoCobro;
    }
    
    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }
    
    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }
    
    public String getTitularTarjeta() {
        return titularTarjeta;
    }
    
    public void setTitularTarjeta(String titularTarjeta) {
        this.titularTarjeta = titularTarjeta;
    }
    
    public String getIban() {
        return iban;
    }
    
    public void setIban(String iban) {
        this.iban = iban;
    }
}
