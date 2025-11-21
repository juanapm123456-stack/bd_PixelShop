# 💰 SISTEMA DE PAGOS Y COMISIONES - PixelShop

**Modelo de Negocio y Flujo Financiero**

---

## 📋 Contenido

1. [Resumen del Modelo](#-resumen-del-modelo)
2. [Actores del Sistema](#-actores-del-sistema)
3. [Flujo de Dinero](#-flujo-de-dinero)
4. [Comisiones Detalladas](#-comisiones-detalladas)
5. [Ejemplos Prácticos](#-ejemplos-prácticos)
6. [Cálculos Implementados](#-cálculos-implementados)
7. [Casos de Uso Financieros](#-casos-de-uso-financieros)

---

## 💡 Resumen del Modelo

PixelShop es una plataforma de marketplace que conecta proveedores de videojuegos con compradores, obteniendo ingresos por:

### Fuentes de Ingresos de la Plataforma

1. **Comisión por Venta**: 15% del precio de cada juego vendido
2. **Costo de Publicación**: 25€ por cada juego publicado

### Distribución del Dinero

```
Venta de 100€
├─ 85€  → PROVEEDOR (85%)
└─ 15€  → PLATAFORMA (15%)

Publicación
└─ 25€  → PLATAFORMA (costo fijo)
```

---

## 👥 Actores del Sistema

### 1. PLATAFORMA (PixelShop)

**Rol:** Intermediario que facilita las transacciones.

**Ingresos:**
- ✅ 15% de cada venta
- ✅ 25€ por cada publicación

**Responsabilidades:**
- Gestionar el catálogo de juegos
- Procesar compras
- Distribuir comisiones a proveedores

---

### 2. PROVEEDOR (Publisher/Developer)

**Rol:** Creadores o distribuidores de videojuegos.

**Ingresos:**
- ✅ 85% del precio de cada venta de sus juegos

**Gastos:**
- ❌ 25€ por publicar cada juego

**Ejemplo:**
```
Proveedor publica un juego de 50€
- Paga 25€ de publicación
- Cada venta genera 42.50€ (85% de 50€)
- Necesita 1 venta para recuperar inversión
- A partir de la 2ª venta, todo es ganancia
```

---

### 3. CLIENTE (Customer)

**Rol:** Usuarios que compran juegos.

**Gastos:**
- ❌ Precio completo del juego

**Beneficios:**
- ✅ Juego añadido a su biblioteca personal
- ✅ Sin comisiones adicionales

---

## 💸 Flujo de Dinero

### Escenario 1: Publicación de un Juego

```
┌─────────────────────────────────────────────────────┐
│                   PROVEEDOR                         │
│              Publica un juego                       │
└────────────────────┬────────────────────────────────┘
                     │ Paga 25€
                     ▼
┌─────────────────────────────────────────────────────┐
│                  PLATAFORMA                         │
│           Recibe 25€ (costo publicación)            │
└─────────────────────────────────────────────────────┘

Registro en BD:
- Tabla: publicacion_juego
- Campos: juego_id, proveedor_id, costo (25€), fecha
```

**Código:**
```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(...) {
    // 1. Crear y guardar juego
    Juego juegoGuardado = servicioJuego.guardarJuego(nuevoJuego);
    
    // 2. Registrar publicación (25€)
    PublicacionJuego publicacion = servicioProveedor.registrarPublicacionJuego(
        juegoGuardado, 
        proveedor
    );
    
    // La plataforma recibe 25€
    return "redirect:/proveedor/mis-juegos";
}
```

---

### Escenario 2: Venta de un Juego

```
┌─────────────────────────────────────────────────────┐
│                   CLIENTE                           │
│              Compra juego de 100€                   │
└────────────────────┬────────────────────────────────┘
                     │ Paga 100€
                     ▼
┌─────────────────────────────────────────────────────┐
│                  PLATAFORMA                         │
│              Recibe 100€ totales                    │
│                                                     │
│         Distribuye automáticamente:                 │
│         ├─ 85€ → Proveedor (pendiente)             │
│         └─ 15€ → Plataforma (ganancia)             │
└─────┬──────────────────────────────────────┬────────┘
      │                                      │
      │ 85€ (pendiente)                     │ 15€ (ganancia)
      ▼                                      ▼
┌─────────────────────┐          ┌──────────────────────┐
│    PROVEEDOR        │          │    PLATAFORMA        │
│  Ingreso pendiente  │          │   Ganancia directa   │
└─────────────────────┘          └──────────────────────┘

Registros en BD:
1. Tabla: compras
   - usuario_id, juego_id, precio (100€), fecha, pagada_al_proveedor (false)

2. Tabla: biblioteca_usuario
   - usuario_id, juego_id, fecha_adquisicion

3. Tabla: movimiento_proveedor
   - proveedor_id, compra_id, monto (85€), fecha, cobrado (false)
```

**Código:**
```java
@PostMapping("/comprar/{id}")
public String comprarJuego(@PathVariable Long id, Principal principal) {
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    // Crear compra (esto hace todo automáticamente)
    Compra compra = servicioCompra.crearNuevaCompra(usuario, juego);
    
    // Internamente esto:
    // 1. Guarda la compra (100€)
    // 2. Añade juego a biblioteca del usuario
    // 3. Crea movimiento para proveedor (85€ pendientes)
    // 4. Plataforma se queda con 15€
    
    return "redirect:/usuario/mi-biblioteca";
}
```

---

## 📊 Comisiones Detalladas

### Constantes Definidas en el Código

```java
// En ServicioCompra.java
private static final BigDecimal COMISION_PROVEEDOR = new BigDecimal("0.85");    // 85%
private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.15");   // 15%

// En ServicioProveedor.java
private static final BigDecimal COSTO_PUBLICACION = new BigDecimal("25.00");    // 25€
```

---

### Tabla de Comisiones por Precio

| Precio Juego | Para Proveedor (85%) | Para Plataforma (15%) |
|-------------|---------------------|---------------------|
| 10€         | 8.50€              | 1.50€              |
| 20€         | 17.00€             | 3.00€              |
| 30€         | 25.50€             | 4.50€              |
| 40€         | 34.00€             | 6.00€              |
| 50€         | 42.50€             | 7.50€              |
| 60€         | 51.00€             | 9.00€              |
| 70€         | 59.50€             | 10.50€             |
| 80€         | 68.00€             | 12.00€             |
| 90€         | 76.50€             | 13.50€             |
| 100€        | 85.00€             | 15.00€             |

---

### Fórmulas Matemáticas

#### Monto para el Proveedor
```
Monto Proveedor = Precio × 0.85
```

**Código:**
```java
@Override
public BigDecimal calcularMontoParaProveedor(BigDecimal precio) {
    return precio.multiply(COMISION_PROVEEDOR).setScale(2, RoundingMode.HALF_UP);
}
```

**Ejemplo:**
```java
BigDecimal precio = new BigDecimal("100.00");
BigDecimal montoProveedor = servicioCompra.calcularMontoParaProveedor(precio);
System.out.println(montoProveedor); // 85.00
```

---

#### Comisión de la Plataforma
```
Comisión Plataforma = Precio × 0.15
```

**Código:**
```java
@Override
public BigDecimal calcularComisionPlataforma(BigDecimal precio) {
    return precio.multiply(COMISION_PLATAFORMA).setScale(2, RoundingMode.HALF_UP);
}
```

**Ejemplo:**
```java
BigDecimal precio = new BigDecimal("100.00");
BigDecimal comisionPlataforma = servicioCompra.calcularComisionPlataforma(precio);
System.out.println(comisionPlataforma); // 15.00
```

---

#### Ganancias Totales de la Plataforma
```
Ganancias Totales = (Total Ventas × 0.15) + (Número Publicaciones × 25)
```

**Código:**
```java
@Override
public ResumenGanancias calcularGananciasTotalesPlataforma() {
    // 1. Ganancias por ventas (15%)
    BigDecimal gananciasPorVentas = calcularGananciasPorVentas();
    
    // 2. Ganancias por publicaciones (25€ cada una)
    BigDecimal gananciasPorPublicaciones = calcularGananciasPorPublicaciones();
    
    // 3. Total
    BigDecimal totalGanancias = gananciasPorVentas.add(gananciasPorPublicaciones);
    
    return new ResumenGanancias(
        gananciasPorVentas,
        gananciasPorPublicaciones,
        totalGanancias,
        numeroVentas,
        numeroPublicaciones
    );
}
```

---

## 💼 Ejemplos Prácticos

### Ejemplo 1: Proveedor Publica 3 Juegos

**Datos:**
- Juego A: 30€
- Juego B: 50€
- Juego C: 70€

**Costos de Publicación:**
```
Juego A: 25€
Juego B: 25€
Juego C: 25€
──────────────
Total pagado: 75€
```

**Ingresos de la Plataforma (solo por publicaciones):**
```
3 juegos × 25€ = 75€
```

---

### Ejemplo 2: Ventas del Juego A (30€)

**Escenario:** Se venden 10 copias del Juego A (30€)

**Cálculos por venta:**
```
Precio: 30€
├─ Para proveedor (85%): 25.50€
└─ Para plataforma (15%): 4.50€
```

**Total después de 10 ventas:**
```
Proveedor recibe:    10 × 25.50€ = 255.00€
Plataforma recibe:   10 × 4.50€  = 45.00€
───────────────────────────────────────────
Total ventas:                      300.00€
```

**Balance del Proveedor:**
```
Ingresos por ventas:     +255.00€
Costo de publicación:    -25.00€
─────────────────────────────────
Ganancia neta:           +230.00€
```

---

### Ejemplo 3: Plataforma con Múltiples Proveedores

**Escenario:**
- 50 juegos publicados
- 200 ventas totales
- Precio promedio: 40€

**Ganancias por Publicaciones:**
```
50 juegos × 25€ = 1,250€
```

**Ganancias por Ventas:**
```
Total vendido:     200 × 40€ = 8,000€
Comisión 15%:      8,000€ × 0.15 = 1,200€
```

**Ganancias Totales de la Plataforma:**
```
Publicaciones:     1,250€
Ventas (15%):      1,200€
─────────────────────────
Total:             2,450€
```

**Ingresos de los Proveedores (85%):**
```
Total vendido:     8,000€
Comisión 85%:      8,000€ × 0.85 = 6,800€
Menos publicaciones: 6,800€ - 1,250€ = 5,550€
```

---

### Ejemplo 4: Caso Real Completo

**Proveedor "GameStudio":**
- Publica 5 juegos (costo: 5 × 25€ = 125€)
- Precios: 20€, 30€, 40€, 50€, 60€

**Ventas del primer mes:**
- Juego de 20€: 5 ventas
- Juego de 30€: 8 ventas
- Juego de 40€: 12 ventas
- Juego de 50€: 6 ventas
- Juego de 60€: 3 ventas

**Cálculo de Ingresos del Proveedor:**

```
Juego 20€: 5 ventas × (20€ × 0.85) = 5 × 17.00€ = 85.00€
Juego 30€: 8 ventas × (30€ × 0.85) = 8 × 25.50€ = 204.00€
Juego 40€: 12 ventas × (40€ × 0.85) = 12 × 34.00€ = 408.00€
Juego 50€: 6 ventas × (50€ × 0.85) = 6 × 42.50€ = 255.00€
Juego 60€: 3 ventas × (60€ × 0.85) = 3 × 51.00€ = 153.00€
────────────────────────────────────────────────────────────
Total ingresos por ventas:                          1,105.00€
Menos costo de publicaciones:                        -125.00€
────────────────────────────────────────────────────────────
Ganancia neta del proveedor:                         980.00€
```

**Cálculo de Ingresos de la Plataforma:**

```
Por publicaciones:  5 juegos × 25€ = 125.00€

Por ventas (15%):
- Juego 20€: 5 × 3.00€   = 15.00€
- Juego 30€: 8 × 4.50€   = 36.00€
- Juego 40€: 12 × 6.00€  = 72.00€
- Juego 50€: 6 × 7.50€   = 45.00€
- Juego 60€: 3 × 9.00€   = 27.00€
                Total:     195.00€

────────────────────────────────────
Ganancias totales plataforma: 320.00€
```

**Verificación (total vendido):**
```
Proveedor:   980.00€ + 125.00€ (publicaciones) = 1,105.00€
Plataforma:  320.00€
────────────────────────────────────────────────────────────
Total:       1,425.00€ ✓ (coincide con ventas totales)

Ventas totales:
(5×20€) + (8×30€) + (12×40€) + (6×50€) + (3×60€)
= 100€ + 240€ + 480€ + 300€ + 180€ 
= 1,300€ en ventas + 125€ en publicaciones
= 1,425€ ✓
```

---

## 🔧 Cálculos Implementados

### 1. Calcular Ganancias por Ventas (Plataforma)

**Código:**
```java
@Override
public BigDecimal calcularGananciasPorVentas() {
    // Obtener total de todas las ventas
    BigDecimal totalVentas = servicioCompra.calcularTotalVentas();
    
    // Calcular 15% del total
    return servicioCompra.calcularComisionPlataforma(totalVentas);
}
```

**SQL equivalente:**
```sql
SELECT SUM(precio) * 0.15 
FROM compras 
WHERE usuario_rol != 'ADMIN';
```

---

### 2. Calcular Ganancias por Publicaciones (Plataforma)

**Código:**
```java
@Override
public BigDecimal calcularGananciasPorPublicaciones() {
    // Contar todas las publicaciones
    Long numeroPublicaciones = servicioProveedor.contarPublicacionesPagadas();
    
    // Multiplicar por 25€
    BigDecimal costoPublicacion = servicioProveedor.obtenerCostoPublicacion();
    return costoPublicacion.multiply(new BigDecimal(numeroPublicaciones));
}
```

**SQL equivalente:**
```sql
SELECT COUNT(*) * 25 
FROM publicacion_juego;
```

---

### 3. Calcular Ingresos Pendientes (Proveedor)

**Código:**
```java
@Override
public BigDecimal calcularIngresosPendientes(Usuario proveedor) {
    List<MovimientoProveedor> movimientosPendientes = 
        movimientoProveedorRepository.findByProveedorAndCobradoFalse(proveedor);
    
    return movimientosPendientes.stream()
        .map(MovimientoProveedor::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**SQL equivalente:**
```sql
SELECT SUM(monto) 
FROM movimiento_proveedor 
WHERE proveedor_id = ? AND cobrado = false;
```

---

### 4. Crear Movimiento de Proveedor

**Código:**
```java
@Override
public MovimientoProveedor crearMovimientoProveedor(Usuario proveedor, 
                                                    Compra compra, 
                                                    BigDecimal montoNeto) {
    MovimientoProveedor movimiento = new MovimientoProveedor();
    movimiento.setProveedor(proveedor);
    movimiento.setCompra(compra);
    movimiento.setMonto(montoNeto); // 85% del precio
    movimiento.setFecha(LocalDateTime.now());
    movimiento.setCobrado(false); // Pendiente de cobro
    
    return movimientoProveedorRepository.save(movimiento);
}
```

**Ejemplo:**
```java
// Al crear una compra de 100€
BigDecimal montoProveedor = new BigDecimal("100.00")
    .multiply(new BigDecimal("0.85")); // 85.00€

MovimientoProveedor movimiento = servicioProveedor.crearMovimientoProveedor(
    proveedor,
    compra,
    montoProveedor // 85.00€
);
```

---

## 🎯 Casos de Uso Financieros

### Caso 1: Dashboard de Ganancias (Admin)

**Vista del Administrador:**
```java
@GetMapping("/admin/ganancias")
public String verGanancias(Model model) {
    
    // Obtener resumen completo
    ResumenGanancias resumen = servicioAdministrador.calcularGananciasTotalesPlataforma();
    
    model.addAttribute("gananciasPorVentas", resumen.getGananciasPorVentas());
    model.addAttribute("gananciasPorPublicaciones", resumen.getGananciasPorPublicaciones());
    model.addAttribute("totalGanancias", resumen.getTotalGanancias());
    model.addAttribute("numeroVentas", resumen.getNumeroVentas());
    model.addAttribute("numeroPublicaciones", resumen.getNumeroPublicaciones());
    
    return "admin/ganancias";
}
```

**Vista Thymeleaf (admin/ganancias.html):**
```html
<div class="dashboard">
    <h1>Ganancias de la Plataforma</h1>
    
    <div class="card">
        <h3>Ganancias por Ventas (15%)</h3>
        <p class="amount">€[[${gananciasPorVentas}]]</p>
        <p class="detail">[[${numeroVentas}]] ventas realizadas</p>
    </div>
    
    <div class="card">
        <h3>Ganancias por Publicaciones (25€)</h3>
        <p class="amount">€[[${gananciasPorPublicaciones}]]</p>
        <p class="detail">[[${numeroPublicaciones}]] juegos publicados</p>
    </div>
    
    <div class="card total">
        <h3>Total Ganancias</h3>
        <p class="amount">€[[${totalGanancias}]]</p>
    </div>
</div>
```

---

### Caso 2: Panel de Ventas (Proveedor)

**Vista del Proveedor:**
```java
@GetMapping("/proveedor/ventas")
public String verVentas(Principal principal, Model model) {
    
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // Obtener movimientos
    List<MovimientoProveedor> movimientos = 
        servicioProveedor.obtenerMovimientosDeProveedor(proveedor);
    
    // Calcular pendiente
    BigDecimal pendiente = servicioProveedor.calcularIngresosPendientes(proveedor);
    
    // Calcular cobrado
    BigDecimal cobrado = movimientos.stream()
        .filter(MovimientoProveedor::getCobrado)
        .map(MovimientoProveedor::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    // Total generado
    BigDecimal totalGenerado = pendiente.add(cobrado);
    
    model.addAttribute("movimientos", movimientos);
    model.addAttribute("pendiente", pendiente);
    model.addAttribute("cobrado", cobrado);
    model.addAttribute("totalGenerado", totalGenerado);
    
    return "proveedor/ventas";
}
```

**Vista Thymeleaf (proveedor/ventas.html):**
```html
<div class="ventas-dashboard">
    <h1>Mis Ventas</h1>
    
    <div class="resumen">
        <div class="card pendiente">
            <h3>Pendiente de Cobro</h3>
            <p class="amount">€[[${pendiente}]]</p>
        </div>
        
        <div class="card cobrado">
            <h3>Ya Cobrado</h3>
            <p class="amount">€[[${cobrado}]]</p>
        </div>
        
        <div class="card total">
            <h3>Total Generado</h3>
            <p class="amount">€[[${totalGenerado}]]</p>
        </div>
    </div>
    
    <table>
        <thead>
            <tr>
                <th>Fecha</th>
                <th>Juego</th>
                <th>Precio Venta</th>
                <th>Tu Parte (85%)</th>
                <th>Estado</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="movimiento : ${movimientos}">
                <td th:text="${#temporals.format(movimiento.fecha, 'dd/MM/yyyy')}"></td>
                <td th:text="${movimiento.compra.juego.titulo}"></td>
                <td th:text="'€' + ${movimiento.compra.precio}"></td>
                <td th:text="'€' + ${movimiento.monto}"></td>
                <td>
                    <span th:if="${movimiento.cobrado}" class="badge success">Cobrado</span>
                    <span th:unless="${movimiento.cobrado}" class="badge pending">Pendiente</span>
                </td>
            </tr>
        </tbody>
    </table>
</div>
```

---

### Caso 3: Historial de Compras (Cliente)

**Vista del Cliente:**
```java
@GetMapping("/usuario/mis-compras")
public String verMisCompras(Principal principal, Model model) {
    
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // Obtener compras
    List<Compra> compras = servicioCompra.obtenerComprasDeUsuario(usuario);
    
    // Calcular total gastado
    BigDecimal totalGastado = compras.stream()
        .map(Compra::getPrecio)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    model.addAttribute("compras", compras);
    model.addAttribute("totalCompras", compras.size());
    model.addAttribute("totalGastado", totalGastado);
    
    return "usuario/mis-compras";
}
```

**Vista Thymeleaf (usuario/mis-compras.html):**
```html
<div class="mis-compras">
    <h1>Mis Compras</h1>
    
    <div class="resumen">
        <p>Total de compras: [[${totalCompras}]]</p>
        <p>Total gastado: €[[${totalGastado}]]</p>
    </div>
    
    <table>
        <thead>
            <tr>
                <th>Fecha</th>
                <th>Juego</th>
                <th>Precio</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="compra : ${compras}">
                <td th:text="${#temporals.format(compra.fechaCompra, 'dd/MM/yyyy HH:mm')}"></td>
                <td>
                    <a th:href="@{/juego/{id}(id=${compra.juego.id})}" 
                       th:text="${compra.juego.titulo}"></a>
                </td>
                <td th:text="'€' + ${compra.precio}"></td>
            </tr>
        </tbody>
    </table>
</div>
```

---

## 📝 Reglas de Negocio Financieras

### ✅ Reglas Implementadas

1. **Comisión Fija**
   - Plataforma: 15%
   - Proveedor: 85%
   - No son negociables

2. **Costo de Publicación**
   - 25€ por cada juego
   - Se paga al publicar, no al vender

3. **Sin Doble Compra**
   - Un usuario no puede comprar el mismo juego dos veces
   - Validación antes de crear la compra

4. **Admin No Compra**
   - Los administradores no pueden comprar juegos
   - Solo gestionan la plataforma

5. **Movimientos Pendientes**
   - Los ingresos del proveedor se marcan como "pendientes"
   - Se pueden marcar como "cobrados" manualmente

6. **Biblioteca Automática**
   - Al comprar, el juego se añade automáticamente a la biblioteca
   - Sin pasos adicionales

---

## 🎯 Resumen del Flujo Financiero

```
PUBLICACIÓN:
Proveedor → 25€ → Plataforma

VENTA:
Cliente → Precio Completo → Sistema
    ├─ 85% → Proveedor (pendiente de cobro)
    └─ 15% → Plataforma (ganancia inmediata)

GANANCIAS PLATAFORMA:
├─ Por Ventas: Total Ventas × 15%
└─ Por Publicaciones: Número Publicaciones × 25€
```

---

**El sistema de comisiones está completamente automatizado y garantiza la correcta distribución del dinero entre plataforma y proveedores.** 💰

Para más información, consulta:
- [ARQUITECTURA.md](ARQUITECTURA.md) - Arquitectura del sistema
- [SERVICIOS.md](SERVICIOS.md) - Servicios implementados
- [SEGURIDAD.md](SEGURIDAD.md) - Spring Security
