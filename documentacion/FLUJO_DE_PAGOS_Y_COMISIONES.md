# 📊 DOCUMENTACIÓN: FLUJO DE PAGOS Y COMISIONES - PIXELSHOP

## 📋 ÍNDICE
1. [Descripción General del Sistema](#descripción-general-del-sistema)
2. [Modelo de Negocio](#modelo-de-negocio)
3. [Arquitectura del Sistema de Pagos](#arquitectura-del-sistema-de-pagos)
4. [Flujo Completo de Transacciones](#flujo-completo-de-transacciones)
5. [Implementación Técnica Detallada](#implementación-técnica-detallada)
6. [Cálculo de Ganancias de la Plataforma](#cálculo-de-ganancias-de-la-plataforma)
7. [Validaciones y Reglas de Negocio](#validaciones-y-reglas-de-negocio)

---

## 🎮 DESCRIPCIÓN GENERAL DEL SISTEMA

**PixelShop** es una plataforma de comercio electrónico para la venta de videojuegos digitales, similar a Steam o Epic Games Store. La plataforma gestiona dos tipos principales de transacciones monetarias:

1. **Publicación de juegos** por parte de proveedores
2. **Compra de juegos** por parte de usuarios finales

---

## 💰 MODELO DE NEGOCIO

La plataforma PixelShop genera ingresos mediante dos mecanismos:

### 1. Tarifa de Publicación
- **Costo:** 25€ por cada juego publicado
- **Responsable:** Proveedor (desarrollador/distribuidor)
- **Momento del pago:** Al publicar un nuevo juego en la plataforma

### 2. Comisión por Ventas
- **Porcentaje:** 15% del precio de venta
- **Distribución del dinero:**
  - **85%** va para el proveedor (desarrollador)
  - **15%** se queda la plataforma (PixelShop)
- **Momento del pago:** Cuando un usuario compra el juego

### Ejemplo Práctico
Si un juego cuesta **50€**:
- Usuario paga: **50€**
- Proveedor recibe: **42.50€** (85% de 50€)
- Plataforma recibe: **7.50€** (15% de 50€)

Adicionalmente, el proveedor pagó **25€** al publicar el juego inicialmente.

---

## 🏗️ ARQUITECTURA DEL SISTEMA DE PAGOS

### Entidades Principales

El sistema utiliza 4 entidades clave para gestionar el flujo de pagos:

#### 1. **PublicacionJuego**
Registra la publicación de un juego y la tarifa de 25€.

**Ubicación:** `src/main/java/com/example/model/PublicacionJuego.java`

**Campos relevantes:**
```java
@Column(nullable = false)
private Boolean pagado = false; // 25€
```
- **Líneas:** 20-21

#### 2. **Compra**
Registra cada transacción de compra de un juego por un usuario.

**Ubicación:** `src/main/java/com/example/model/Compra.java`

**Campos relevantes:**
```java
@Column(name = "precio_pagado", nullable = false, precision = 10, scale = 2)
private BigDecimal precioPagado;

@Column(name = "pagado_al_proveedor", nullable = false)
private Boolean pagadoAlProveedor = false;
```
- **Líneas:** 23-26

#### 3. **MovimientoProveedor**
Registra los pagos pendientes/realizados hacia los proveedores (85% de cada venta).

**Ubicación:** `src/main/java/com/example/model/MovimientoProveedor.java`

**Campos relevantes:**
```java
@Column(name = "monto_neto", nullable = false, precision = 10, scale = 2)
private BigDecimal montoNeto; // 85% del precio

@Column(nullable = false)
private Boolean pagado = false;
```
- **Líneas:** 20-24

#### 4. **Juego**
Entidad principal que representa el producto (videojuego).

**Ubicación:** `src/main/java/com/example/model/Juego.java`

**Campos relevantes:**
```java
@Column(nullable = false, precision = 10, scale = 2)
private BigDecimal precio;

@ManyToOne
@JoinColumn(name = "proveedor_id", nullable = false)
private Usuario proveedor;
```
- **Líneas:** 20-25

---

## 🔄 FLUJO COMPLETO DE TRANSACCIONES

### FLUJO 1: Publicación de un Juego (Cobro de 25€)

```
┌─────────────┐      ┌──────────────────┐      ┌────────────────┐
│  Proveedor  │─────>│  Publicar Juego  │─────>│  Cobro de 25€  │
└─────────────┘      └──────────────────┘      └────────────────┘
                              │
                              v
                     ┌────────────────────┐
                     │  PublicacionJuego  │
                     │   pagado = true    │
                     └────────────────────┘
```

**Controlador:** `ProveedorController.java`
**Método:** `publicarJuego()`
**Ubicación:** `src/main/java/com/example/controller/ProveedorController.java`
**Líneas:** 46-156

**Código clave (líneas 139-146):**
```java
// Registrar PUBLICACION (cobro de 25€)
PublicacionJuego publicacion = new PublicacionJuego();
publicacion.setJuego(juego);
publicacion.setProveedor(proveedor);
publicacion.setFechaPublicacion(LocalDateTime.now());
publicacion.setPagado(true); // Simular pago de 25€
publicacionRepository.save(publicacion);
```

**Mensaje de confirmación (línea 148):**
```java
redirectAttributes.addFlashAttribute("success", "¡Juego publicado exitosamente! (25€ cobrados)");
```

### FLUJO 2: Compra de un Juego (Distribución 85%-15%)

```
┌──────────┐      ┌──────────────┐      ┌────────────────────┐
│ Usuario  │─────>│ Comprar Juego│─────>│ Usuario paga 100%  │
└──────────┘      └──────────────┘      └────────────────────┘
                          │
          ┌───────────────┴───────────────┐
          v                               v
┌─────────────────────┐       ┌────────────────────────┐
│  Crear Compra       │       │ Añadir a Biblioteca    │
│  precio_pagado=100% │       │ del Usuario            │
└─────────────────────┘       └────────────────────────┘
          │
          v
┌──────────────────────────┐
│ MovimientoProveedor      │
│ monto_neto = 85%         │
│ pagado = false           │
└──────────────────────────┘
          │
          v
  ┌─────────────────┐
  │ Ganancia de la  │
  │ Plataforma: 15% │
  └─────────────────┘
```

**Controlador:** `CompraController.java`
**Método:** `comprarJuego()`
**Ubicación:** `src/main/java/com/example/controller/CompraController.java`
**Líneas:** 28-84

#### Pasos Detallados:

##### 1. Validación de Usuario Admin (líneas 36-40)
```java
// ✅ VALIDACIÓN CRÍTICA: ADMIN NO PUEDE COMPRAR
if (usuario.getRol() == Rol.ADMIN) {
    redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
    return "redirect:/juego/" + juegoId;
}
```

##### 2. Verificación de Compra Duplicada (líneas 46-49)
```java
// Verificar que no haya comprado ya este juego
if (compraRepository.existsByUsuarioAndJuego(usuario, juego)) {
    redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
    return "redirect:/juego/" + juegoId;
}
```

##### 3. Creación del Registro de Compra (líneas 51-58)
```java
// Crear COMPRA
Compra compra = new Compra();
compra.setUsuario(usuario);
compra.setJuego(juego);
compra.setPrecioPagado(juego.getPrecio());  // 100% del precio
compra.setFechaCompra(LocalDateTime.now());
compra.setPagadoAlProveedor(false);
compraRepository.save(compra);
```

##### 4. Añadir Juego a Biblioteca del Usuario (líneas 60-65)
```java
// Añadir a BIBLIOTECA
BibliotecaUsuario biblioteca = new BibliotecaUsuario();
biblioteca.setUsuario(usuario);
biblioteca.setJuego(juego);
biblioteca.setFechaAdquisicion(LocalDateTime.now());
bibliotecaRepository.save(biblioteca);
```

##### 5. **CÁLCULO DE COMISIÓN Y CREACIÓN DE MOVIMIENTO** (líneas 67-75)
```java
// Crear MOVIMIENTO_PROVEEDOR (85% al proveedor)
BigDecimal montoProveedor = juego.getPrecio().multiply(new BigDecimal("0.85"));
MovimientoProveedor movimiento = new MovimientoProveedor();
movimiento.setProveedor(juego.getProveedor());
movimiento.setCompra(compra);
movimiento.setMontoNeto(montoProveedor);  // 85% del precio
movimiento.setPagado(false);
movimiento.setFecha(LocalDateTime.now());
movimientoRepository.save(movimiento);
```

**🔑 PUNTO CRÍTICO:** En la línea 68 se realiza el cálculo más importante:
```java
BigDecimal montoProveedor = juego.getPrecio().multiply(new BigDecimal("0.85"));
```
Esto calcula el **85%** del precio que recibirá el proveedor. El **15%** restante se queda implícitamente en la plataforma (diferencia entre `precioPagado` y `montoNeto`).

---

## 🔧 IMPLEMENTACIÓN TÉCNICA DETALLADA

### Repositorios y Consultas SQL

#### 1. CompraRepository
**Ubicación:** `src/main/java/com/example/repository/CompraRepository.java`

**Consulta clave para calcular ventas totales (líneas 15-16):**
```java
@Query("SELECT SUM(c.precioPagado) FROM Compra c")
BigDecimal calcularTotalVentas();
```

Esta query suma todos los `precioPagado` de la tabla `compras`, que representa el **100%** del dinero que entra a la plataforma.

#### 2. MovimientoProveedorRepository
**Ubicación:** `src/main/java/com/example/repository/MovimientoProveedorRepository.java`

**Consulta para calcular ingresos pendientes del proveedor (líneas 14-15):**
```java
@Query("SELECT SUM(m.montoNeto) FROM MovimientoProveedor m WHERE m.proveedor = :proveedor AND m.pagado = false")
BigDecimal calcularIngresosPendientes(@Param("proveedor") Usuario proveedor);
```

Esta query suma todos los `montoNeto` (85% de cada venta) que aún no han sido pagados al proveedor.

#### 3. PublicacionJuegoRepository
**Ubicación:** `src/main/java/com/example/repository/PublicacionJuegoRepository.java`

**Consulta para contar publicaciones pagadas (líneas 13-14):**
```java
@Query("SELECT COUNT(p) FROM PublicacionJuego p WHERE p.pagado = true")
Long contarPublicacionesPagadas();
```

Cuenta cuántas publicaciones de juegos se han realizado (cada una genera 25€ para la plataforma).

---

## 💵 CÁLCULO DE GANANCIAS DE LA PLATAFORMA

### Panel de Administración

**Controlador:** `AdminController.java`
**Método:** `ganancias()`
**Ubicación:** `src/main/java/com/example/controller/AdminController.java`
**Líneas:** 36-54

```java
@GetMapping("/ganancias")
public String ganancias(Model model) {
    // Calcular ganancias totales
    BigDecimal totalVentas = compraRepository.calcularTotalVentas();
    BigDecimal gananciaVentas = totalVentas != null ? totalVentas.multiply(new BigDecimal("0.15")) : BigDecimal.ZERO;
    
    Long totalPublicaciones = publicacionRepository.contarPublicacionesPagadas();
    BigDecimal gananciaPublicaciones = new BigDecimal(totalPublicaciones != null ? totalPublicaciones : 0)
        .multiply(new BigDecimal("25"));
    
    BigDecimal gananciaTotal = gananciaVentas.add(gananciaPublicaciones);
    
    model.addAttribute("gananciaVentas", gananciaVentas);
    model.addAttribute("gananciaPublicaciones", gananciaPublicaciones);
    model.addAttribute("gananciaTotal", gananciaTotal);
    model.addAttribute("totalVentas", totalVentas);
    model.addAttribute("totalPublicaciones", totalPublicaciones);
    
    return "admin/ganancias";
}
```

### Desglose del Cálculo (línea por línea)

#### Línea 39: Suma de todas las ventas
```java
BigDecimal totalVentas = compraRepository.calcularTotalVentas();
```
Obtiene la suma de todos los `precioPagado` (100% de lo que pagaron los usuarios).

#### Línea 40: Cálculo del 15% de comisión
```java
BigDecimal gananciaVentas = totalVentas != null ? totalVentas.multiply(new BigDecimal("0.15")) : BigDecimal.ZERO;
```
Multiplica el total de ventas por **0.15** (15%) para obtener la ganancia por comisiones de venta.

#### Línea 42: Conteo de publicaciones
```java
Long totalPublicaciones = publicacionRepository.contarPublicacionesPagadas();
```
Cuenta cuántos juegos se han publicado (cada uno pagó 25€).

#### Líneas 43-44: Cálculo de ganancias por publicaciones
```java
BigDecimal gananciaPublicaciones = new BigDecimal(totalPublicaciones != null ? totalPublicaciones : 0)
    .multiply(new BigDecimal("25"));
```
Multiplica el número de publicaciones por **25€** para obtener la ganancia total por tarifas de publicación.

#### Línea 46: Suma total de ganancias
```java
BigDecimal gananciaTotal = gananciaVentas.add(gananciaPublicaciones);
```
Suma las ganancias por ventas (15%) + ganancias por publicaciones (25€ cada una).

### Fórmulas Matemáticas

```
Ganancia por Ventas = Σ(precio_pagado) × 0.15

Ganancia por Publicaciones = COUNT(publicaciones) × 25€

Ganancia Total = Ganancia por Ventas + Ganancia por Publicaciones
```

---

## 🛡️ VALIDACIONES Y REGLAS DE NEGOCIO

### 1. Los Administradores NO Pueden Comprar Juegos

**Ubicación:** `CompraController.java`, líneas 36-40
**Razón:** Evitar conflictos de interés y manipulación de estadísticas.

```java
// ✅ VALIDACIÓN CRÍTICA: ADMIN NO PUEDE COMPRAR
if (usuario.getRol() == Rol.ADMIN) {
    redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
    return "redirect:/juego/" + juegoId;
}
```

**Impacto en las compras:** En el método `movimientos()` del `AdminController` (líneas 56-65), se filtran las compras de administradores:

```java
@GetMapping("/movimientos")
public String movimientos(Model model) {
    // Filtrar compras: excluir las de usuarios ADMIN
    List<Compra> todasLasCompras = compraRepository.findAll();
    List<Compra> compras = todasLasCompras.stream()
        .filter(compra -> !compra.getUsuario().getRol().name().equals("ADMIN"))
        .toList();
    
    model.addAttribute("compras", compras);
    return "admin/movimientos";
}
```

### 2. Unicidad de Compras

**Ubicación:** `Compra.java`, líneas 9-10

```java
@Table(name = "compras",
    uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "juego_id"}))
```

**Validación adicional en controlador:** `CompraController.java`, líneas 46-49

```java
if (compraRepository.existsByUsuarioAndJuego(usuario, juego)) {
    redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
    return "redirect:/juego/" + juegoId;
}
```

**Regla:** Un usuario no puede comprar el mismo juego dos veces.

### 3. Campos Automáticos con @PrePersist

#### En Compra.java (líneas 32-39)
```java
@PrePersist
protected void onCreate() {
    if (fechaCompra == null) {
        fechaCompra = LocalDateTime.now();
    }
    if (pagadoAlProveedor == null) {
        pagadoAlProveedor = false;
    }
}
```

#### En MovimientoProveedor.java (líneas 43-50)
```java
@PrePersist
protected void onCreate() {
    if (fecha == null) {
        fecha = LocalDateTime.now();
    }
    if (pagado == null) {
        pagado = false;
    }
}
```

**Propósito:** Garantizar que todos los registros tengan valores iniciales correctos aunque no se especifiquen explícitamente.

### 4. Relación Bidireccional Compra ↔ MovimientoProveedor

**En Compra.java (línea 27):**
```java
@OneToOne(mappedBy = "compra", cascade = CascadeType.ALL)
private MovimientoProveedor movimiento;
```

**En MovimientoProveedor.java (líneas 17-19):**
```java
@OneToOne
@JoinColumn(name = "compra_id", nullable = false)
private Compra compra;
```

**Propósito:** Garantizar trazabilidad completa entre cada compra y el pago correspondiente al proveedor.

---

## 📊 VISTA DEL PROVEEDOR: Panel de Ventas

**Controlador:** `ProveedorController.java`
**Método:** `ventas()`
**Ubicación:** Líneas 254-264

```java
@GetMapping("/ventas")
public String ventas(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    Usuario proveedor = usuarioRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    
    List<MovimientoProveedor> movimientos = movimientoRepository.findByProveedorOrderByFechaDesc(proveedor);
    BigDecimal ingresosPendientes = movimientoRepository.calcularIngresosPendientes(proveedor);
    
    model.addAttribute("movimientos", movimientos);
    model.addAttribute("ingresosPendientes", ingresosPendientes != null ? ingresosPendientes : BigDecimal.ZERO);
    
    return "proveedor/ventas";
}
```

### Funcionalidad de Cobro

**Método:** `cobrarMovimiento()`
**Ubicación:** Líneas 266-274

```java
@PostMapping("/cobrar/{movimientoId}")
public String cobrarMovimiento(@PathVariable Long movimientoId, RedirectAttributes redirectAttributes) {
    MovimientoProveedor movimiento = movimientoRepository.findById(movimientoId)
        .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));
    
    movimiento.setPagado(true);
    movimientoRepository.save(movimiento);
    
    redirectAttributes.addFlashAttribute("success", "Movimiento marcado como cobrado");
    return "redirect:/proveedor/ventas";
}
```

**Función:** Permite al proveedor marcar que ha recibido su pago (85% de una venta específica).

---

## 📐 DIAGRAMA DE FLUJO COMPLETO

```
┌────────────────────────────────────────────────────────────┐
│                    CICLO DE VIDA DEL DINERO                │
└────────────────────────────────────────────────────────────┘

FASE 1: PUBLICACIÓN
────────────────────
Proveedor paga 25€ ──┐
                     │
                     v
         ┌─────────────────────────┐
         │  PublicacionJuego       │
         │  pagado = true          │
         │  ──────────────────     │
         │  Plataforma: +25€       │
         └─────────────────────────┘


FASE 2: VENTA DEL JUEGO
────────────────────────
Usuario compra por 100€ ──┐
                          │
                          v
              ┌─────────────────────┐
              │  Registro de Compra │
              │  precio_pagado=100€ │
              └──────────┬──────────┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
         v                               v
┌──────────────────────┐    ┌────────────────────────┐
│ MovimientoProveedor  │    │  Ganancia Plataforma   │
│ monto_neto = 85€     │    │  15% = 15€             │
│ pagado = false       │    │  (implícito)           │
└──────────────────────┘    └────────────────────────┘
         │
         v
  Proveedor cobra 85€
  (pagado = true)


RESULTADO FINANCIERO
────────────────────
Usuario pagó:        100€
Proveedor recibe:     85€
Plataforma recibe:    15€  (comisión venta)
                   + 25€  (publicación inicial)
                   ─────
Total plataforma:     40€
```

---

## 📝 RESUMEN EJECUTIVO

### Puntos Clave del Sistema

1. **Tarifa Fija de Publicación:** 25€ por juego
   - Se cobra en `ProveedorController.publicarJuego()` (línea 139-146)
   - Se registra en la entidad `PublicacionJuego` con `pagado=true`

2. **Comisión por Venta:** 15% del precio
   - Se calcula en `CompraController.comprarJuego()` (línea 68)
   - Fórmula: `montoProveedor = precio × 0.85`
   - El 15% restante es ganancia implícita de la plataforma

3. **Trazabilidad Completa:**
   - Cada compra genera un `MovimientoProveedor`
   - Relación 1:1 entre `Compra` y `MovimientoProveedor`
   - Flag `pagado` controla si el proveedor ha cobrado

4. **Validaciones Críticas:**
   - Administradores no pueden comprar (`CompraController.java`, línea 36)
   - Un usuario no puede comprar el mismo juego dos veces (constraint de BD + validación)
   - Las compras de admins se filtran en estadísticas (`AdminController.java`, línea 62)

5. **Panel de Ganancias:**
   - Fórmula: `gananciaTotal = (totalVentas × 0.15) + (publicaciones × 25)`
   - Implementado en `AdminController.ganancias()` (líneas 36-54)

---

## 🎯 CONCLUSIÓN

El sistema de PixelShop implementa un modelo de negocio dual:
- **Ingresos recurrentes** a través de comisiones del 15% en cada venta
- **Ingresos por publicación** de 25€ por cada juego nuevo

La arquitectura garantiza:
✅ Trazabilidad completa de todas las transacciones  
✅ Separación clara entre dinero del usuario, proveedor y plataforma  
✅ Validaciones robustas para prevenir fraudes o errores  
✅ Reportes precisos de ganancias para administradores y proveedores  

**Total de clases involucradas:** 7  
**Total de métodos clave:** 5  
**Líneas de código críticas:** ~150 líneas relacionadas con el flujo de pagos

---

**Fecha de documentación:** 19 de noviembre de 2025  
**Versión del proyecto:** Spring CRUD Thymeleaf v21  
**Tecnologías:** Spring Boot, JPA/Hibernate, Thymeleaf, MySQL
