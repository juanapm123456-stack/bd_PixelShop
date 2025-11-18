package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "biblioteca_usuario",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "juego_id"}))
public class BibliotecaUsuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;
    
    @Column(name = "fecha_adquisicion", nullable = false)
    private LocalDateTime fechaAdquisicion;
    
    // Constructores
    public BibliotecaUsuario() {}
    
    public BibliotecaUsuario(Usuario usuario, Juego juego) {
        this.usuario = usuario;
        this.juego = juego;
        this.fechaAdquisicion = LocalDateTime.now();
    }
    
    // Método para establecer fecha de adquisición automáticamente
    @PrePersist
    protected void onCreate() {
        if (fechaAdquisicion == null) {
            fechaAdquisicion = LocalDateTime.now();
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
    
    public LocalDateTime getFechaAdquisicion() {
        return fechaAdquisicion;
    }
    
    public void setFechaAdquisicion(LocalDateTime fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }
}
