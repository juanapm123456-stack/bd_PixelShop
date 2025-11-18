package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    
    @Column(name = "datos_fiscales", length = 500)
    private String datosFiscales;
    
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Compra> compras = new ArrayList<>();
    
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<Juego> juegosPublicados = new ArrayList<>();
    
    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL)
    private List<MovimientoProveedor> movimientos = new ArrayList<>();
    
    // Constructores
    public Usuario() {}
    
    public Usuario(String nombre, String email, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
    }
    
    public Usuario(String nombre, String email, String password, Rol rol, String datosFiscales) {
        this(nombre, email, password, rol);
        this.datosFiscales = datosFiscales;
    }
    
    // Método para establecer fecha de registro automáticamente
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
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
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public String getDatosFiscales() {
        return datosFiscales;
    }
    
    public void setDatosFiscales(String datosFiscales) {
        this.datosFiscales = datosFiscales;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public List<Compra> getCompras() {
        return compras;
    }
    
    public void setCompras(List<Compra> compras) {
        this.compras = compras;
    }
    
    public List<Juego> getJuegosPublicados() {
        return juegosPublicados;
    }
    
    public void setJuegosPublicados(List<Juego> juegosPublicados) {
        this.juegosPublicados = juegosPublicados;
    }
    
    public List<MovimientoProveedor> getMovimientos() {
        return movimientos;
    }
    
    public void setMovimientos(List<MovimientoProveedor> movimientos) {
        this.movimientos = movimientos;
    }
}
