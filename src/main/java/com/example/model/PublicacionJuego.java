package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publicacion_juego")
public class PublicacionJuego {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "juego_id", unique = true, nullable = false)
    private Juego juego;
    
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Usuario proveedor;
    
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;
    
    @Column(nullable = false)
    private Boolean pagado = false; // 25€
    
    // Constructores
    public PublicacionJuego() {}
    
    public PublicacionJuego(Juego juego, Usuario proveedor) {
        this.juego = juego;
        this.proveedor = proveedor;
        this.fechaPublicacion = LocalDateTime.now();
        this.pagado = false;
    }
    
    public PublicacionJuego(Juego juego, Usuario proveedor, Boolean pagado) {
        this(juego, proveedor);
        this.pagado = pagado;
    }
    
    // Método para establecer fecha de publicación automáticamente
    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
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
    
    public Juego getJuego() {
        return juego;
    }
    
    public void setJuego(Juego juego) {
        this.juego = juego;
    }
    
    public Usuario getProveedor() {
        return proveedor;
    }
    
    public void setProveedor(Usuario proveedor) {
        this.proveedor = proveedor;
    }
    
    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }
    
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
    
    public Boolean getPagado() {
        return pagado;
    }
    
    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }
}
