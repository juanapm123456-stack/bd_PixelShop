package com.example.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "juegos")
public class Juego {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Usuario proveedor;
    
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;
    
    @Column(length = 100)
    private String genero;
    
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
    
    @OneToMany(mappedBy = "juego", cascade = CascadeType.ALL)
    private List<BibliotecaUsuario> bibliotecas = new ArrayList<>();
    
    @OneToOne(mappedBy = "juego", cascade = CascadeType.ALL)
    private PublicacionJuego publicacion;
    
    // Constructores
    public Juego() {}
    
    public Juego(String titulo, String descripcion, BigDecimal precio, Usuario proveedor) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.proveedor = proveedor;
        this.fechaPublicacion = LocalDateTime.now();
        this.activo = true;
    }
    
    public Juego(String titulo, String descripcion, BigDecimal precio, Usuario proveedor, String imagenUrl, String genero) {
        this(titulo, descripcion, precio, proveedor);
        this.imagenUrl = imagenUrl;
        this.genero = genero;
    }
    
    // Método para establecer fecha de publicación automáticamente
    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
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
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public List<Compra> getCompras() {
        return compras;
    }
    
    public void setCompras(List<Compra> compras) {
        this.compras = compras;
    }
    
    public List<BibliotecaUsuario> getBibliotecas() {
        return bibliotecas;
    }
    
    public void setBibliotecas(List<BibliotecaUsuario> bibliotecas) {
        this.bibliotecas = bibliotecas;
    }
    
    public PublicacionJuego getPublicacion() {
        return publicacion;
    }
    
    public void setPublicacion(PublicacionJuego publicacion) {
        this.publicacion = publicacion;
    }
}
