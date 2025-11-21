# 💰 FLUJO TÉCNICO DE PAGOS Y COMISIONES

**Explicación Paso a Paso del Código - Para Tontos**

---

## 📋 Contenido

1. [Resumen Simple](#-resumen-simple)
2. [Flujo 1: Usuario Compra un Juego](#-flujo-1-usuario-compra-un-juego)
3. [Flujo 2: Proveedor Publica un Juego](#-flujo-2-proveedor-publica-un-juego)
4. [Flujo 3: Calcular Ganancias de la Plataforma](#-flujo-3-calcular-ganancias-de-la-plataforma)
5. [Flujo 4: Calcular Ingresos del Proveedor](#-flujo-4-calcular-ingresos-del-proveedor)
6. [Clases y Métodos Involucrados](#-clases-y-métodos-involucrados)

---

## 🎯 Resumen Simple

### ¿Cómo funciona el dinero en PixelShop?

```
1. PUBLICACIÓN (Proveedor paga 25€)
   Proveedor → 25€ → Plataforma

2. COMPRA (Cliente paga precio completo)
   Cliente → 100€ → Sistema
   Sistema divide:
   ├─ 85€ → Proveedor (pendiente)
   └─ 15€ → Plataforma (ganancia)

3. CÁLCULO DE GANANCIAS
   Plataforma gana:
   ├─ 15% de cada venta
   └─ 25€ de cada publicación
```

---

## 🛒 Flujo 1: Usuario Compra un Juego

### Paso a Paso del Código

#### **PASO 1: Usuario hace clic en "Comprar"**

**Archivo:** `catalogo/juego-detalle.html`

```html
<form th:action="@{/comprar/{id}(id=${juego.id})}" method="post">
    <button type="submit">Comprar - €[[${juego.precio}]]</button>
</form>
```

**¿Qué pasa?**
- Usuario ve el botón con el precio del juego
- Hace clic → envía POST a `/comprar/123`

---

#### **PASO 2: El controlador recibe la petición**

**Archivo:** `CompraController.java`

```java
@PostMapping("/comprar/{id}")
public String comprarJuego(@PathVariable Long id, Principal principal, 
                          RedirectAttributes redirectAttributes) {
    
    // 1. Obtener usuario que está comprando
    Usuario usuario = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // 2. Obtener el juego a comprar
    Juego juego = servicioJuego.buscarJuegoPorId(id);
    
    // 3. VALIDAR: ¿Puede comprar? (NO si es ADMIN)
    if (!servicioCompra.puedeRealizarCompras(usuario)) {
        redirectAttributes.addFlashAttribute("error", 
            "Los administradores no pueden comprar");
        return "redirect:/catalogo";
    }
    
    // 4. VALIDAR: ¿Ya lo compró antes?
    if (servicioCompra.verificarJuegoYaComprado(usuario, juego)) {
        redirectAttributes.addFlashAttribute("error", 
            "Ya tienes este juego");
        return "redirect:/catalogo";
    }
    
    // 5. CREAR LA COMPRA (aquí pasa la magia 🎩✨)
    Compra compra = servicioCompra.crearNuevaCompra(usuario, juego);
    
    redirectAttributes.addFlashAttribute("success", "¡Compra realizada!");
    return "redirect:/usuario/mi-biblioteca";
}
```

**¿Qué hace?**
1. Busca el usuario logueado
2. Busca el juego por ID
3. Valida que puede comprar
4. Valida que no lo compró antes
5. **Llama a `crearNuevaCompra()` ← AQUÍ EMPIEZA LA MAGIA**

---

#### **PASO 3: Crear la compra (método principal)**

**Archivo:** `ServicioCompra.java`

```java
@Override
public Compra crearNuevaCompra(Usuario usuario, Juego juego) {
    
    // ===== PASO 3.1: CREAR EL REGISTRO DE COMPRA =====
    Compra compra = new Compra();
    compra.setUsuario(usuario);              // Quién compró
    compra.setJuego(juego);                  // Qué compró
    compra.setPrecio(juego.getPrecio());     // Cuánto pagó (ej: 100€)
    compra.setFechaCompra(LocalDateTime.now()); // Cuándo
    compra.setPagadaAlProveedor(false);      // Aún no se pagó al proveedor
    
    // Guardar en BD (tabla: compras)
    Compra compraSalvada = compraRepository.save(compra);
    
    
    // ===== PASO 3.2: AÑADIR JUEGO A BIBLIOTECA DEL USUARIO =====
    servicioBiblioteca.agregarJuegoABiblioteca(usuario, juego);
    
    
    // ===== PASO 3.3: CALCULAR COMISIÓN DEL PROVEEDOR (85%) =====
    BigDecimal precioJuego = juego.getPrecio(); // Ej: 100€
    BigDecimal montoProveedor = calcularMontoParaProveedor(precioJuego); // 85€
    
    
    // ===== PASO 3.4: CREAR MOVIMIENTO FINANCIERO PARA EL PROVEEDOR =====
    servicioProveedor.crearMovimientoProveedor(
        juego.getProveedor(),    // Proveedor que publicó el juego
        compraSalvada,           // Compra que se acaba de hacer
        montoProveedor           // 85€ que le corresponden
    );
    
    
    // ===== PASO 3.5: LA PLATAFORMA SE QUEDA CON EL 15% AUTOMÁTICAMENTE =====
    // No hace falta guardar nada, el 15% se calcula después con:
    // totalVentas × 0.15
    
    return compraSalvada;
}
```

**¿Qué hace este método?**
1. **Crea el registro de compra** (tabla `compras`)
2. **Añade el juego a la biblioteca** del usuario (tabla `biblioteca_usuario`)
3. **Calcula el 85%** que le toca al proveedor
4. **Crea un movimiento financiero** para el proveedor (tabla `movimiento_proveedor`)
5. **El 15% queda para la plataforma** (se calcula después sumando todas las compras)

---

#### **PASO 3.2: Añadir juego a biblioteca**

**Archivo:** `ServicioBiblioteca.java`

```java
@Override
public BibliotecaUsuario agregarJuegoABiblioteca(Usuario usuario, Juego juego) {
    
    // Crear registro de biblioteca
    BibliotecaUsuario biblioteca = new BibliotecaUsuario();
    biblioteca.setUsuario(usuario);
    biblioteca.setJuego(juego);
    biblioteca.setFechaAdquisicion(LocalDateTime.now());
    
    // Guardar en BD (tabla: biblioteca_usuario)
    return bibliotecaUsuarioRepository.save(biblioteca);
}
```

**Resultado en BD:**
```sql
INSERT INTO biblioteca_usuario (usuario_id, juego_id, fecha_adquisicion)
VALUES (5, 123, '2025-11-21 10:30:00');
```

---

#### **PASO 3.3: Calcular 85% para el proveedor**

**Archivo:** `ServicioCompra.java`

```java
// Constante definida al inicio de la clase
private static final BigDecimal COMISION_PROVEEDOR = new BigDecimal("0.85");

@Override
public BigDecimal calcularMontoParaProveedor(BigDecimal precioJuego) {
    // Ejemplo: 100€ × 0.85 = 85€
    return precioJuego.multiply(COMISION_PROVEEDOR)
                      .setScale(2, RoundingMode.HALF_UP);
}
```

**Ejemplo:**
```
Precio juego: 100.00€
Cálculo: 100.00 × 0.85 = 85.00€
Resultado: 85.00€ para el proveedor
```

---

#### **PASO 3.4: Crear movimiento para el proveedor**

**Archivo:** `ServicioProveedor.java`

```java
@Override
public MovimientoProveedor crearMovimientoProveedor(Usuario proveedor, 
                                                    Compra compra, 
                                                    BigDecimal montoNeto) {
    
    // Crear el movimiento financiero
    MovimientoProveedor movimiento = new MovimientoProveedor();
    movimiento.setProveedor(proveedor);      // Quién recibe el dinero
    movimiento.setCompra(compra);            // De qué compra viene
    movimiento.setMonto(montoNeto);          // Cuánto (85€)
    movimiento.setFecha(LocalDateTime.now()); // Cuándo
    movimiento.setCobrado(false);            // Aún no cobrado
    
    // Guardar en BD (tabla: movimiento_proveedor)
    return movimientoProveedorRepository.save(movimiento);
}
```

**Resultado en BD:**
```sql
INSERT INTO movimiento_proveedor 
    (proveedor_id, compra_id, monto, fecha, cobrado)
VALUES 
    (10, 456, 85.00, '2025-11-21 10:30:00', false);
```

---

#### **PASO 3.5: ¿Dónde está el 15% de la plataforma?**

**Respuesta:** No se guarda en ningún lado específico.

**¿Por qué?** Porque se calcula después sumando todas las compras:

```java
// En ServicioAdministrador.java
BigDecimal totalVentas = servicioCompra.calcularTotalVentas(); // 1000€
BigDecimal comision15 = totalVentas.multiply(new BigDecimal("0.15")); // 150€
```

**Ejemplo:**
```
Si hay 10 compras de 100€ cada una:
- Total vendido: 1000€
- Para proveedores (85%): 850€ (guardado en movimiento_proveedor)
- Para plataforma (15%): 150€ (calculado después)
```

---

### Resumen del Flujo 1 (Compra)

```
CLIENTE COMPRA JUEGO DE 100€

1. CompraController.comprarJuego()
   └─ Valida usuario y juego
   
2. ServicioCompra.crearNuevaCompra()
   ├─ Guarda compra (100€) en tabla: compras
   │  
   ├─ ServicioBiblioteca.agregarJuegoABiblioteca()
   │  └─ Guarda en tabla: biblioteca_usuario
   │  
   ├─ ServicioCompra.calcularMontoParaProveedor(100€)
   │  └─ Retorna: 85€
   │  
   └─ ServicioProveedor.crearMovimientoProveedor(85€)
      └─ Guarda movimiento (85€) en tabla: movimiento_proveedor

RESULTADO:
✅ Usuario tiene el juego en su biblioteca
✅ Proveedor tiene 85€ pendientes de cobro
✅ Plataforma tiene 15€ de ganancia (se calcula después)
```

---

## 📤 Flujo 2: Proveedor Publica un Juego

### Paso a Paso del Código

#### **PASO 1: Proveedor llena el formulario**

**Archivo:** `proveedor/publicar-juego.html`

```html
<form th:action="@{/proveedor/publicar}" method="post" enctype="multipart/form-data">
    <input type="text" name="titulo" placeholder="Título del juego" required />
    <textarea name="descripcion" placeholder="Descripción" required></textarea>
    <input type="number" name="precio" step="0.01" placeholder="Precio" required />
    <input type="file" name="imagen1" accept="image/*" required />
    
    <button type="submit">Publicar Juego (Costo: 25€)</button>
</form>
```

**¿Qué pasa?**
- Proveedor llena datos del juego
- Hace clic en "Publicar" → envía POST a `/proveedor/publicar`

---

#### **PASO 2: El controlador recibe la petición**

**Archivo:** `ProveedorController.java`

```java
@PostMapping("/proveedor/publicar")
public String publicarJuego(@RequestParam String titulo,
                           @RequestParam String descripcion,
                           @RequestParam BigDecimal precio,
                           @RequestParam String genero,
                           @RequestParam MultipartFile imagen1,
                           Principal principal,
                           RedirectAttributes redirectAttributes) throws IOException {
    
    // 1. Obtener proveedor
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // 2. Validar imagen
    String errorImagen = servicioJuego.validarImagen(imagen1, "Imagen 1");
    if (errorImagen != null) {
        redirectAttributes.addFlashAttribute("error", errorImagen);
        return "redirect:/proveedor/publicar";
    }
    
    // 3. Crear juego
    Juego nuevoJuego = servicioJuego.crearNuevoJuego(
        titulo, descripcion, precio, genero, proveedor
    );
    
    // 4. Guardar imagen
    String urlImagen = servicioJuego.guardarImagenJuego(imagen1, 1);
    nuevoJuego.setImagen1(urlImagen);
    
    // 5. Guardar juego en BD
    Juego juegoGuardado = servicioJuego.guardarJuego(nuevoJuego);
    
    // 6. REGISTRAR PUBLICACIÓN (COSTO 25€)
    PublicacionJuego publicacion = servicioProveedor.registrarPublicacionJuego(
        juegoGuardado, 
        proveedor
    );
    
    redirectAttributes.addFlashAttribute("success", 
        "Juego publicado. Costo: 25€");
    
    return "redirect:/proveedor/mis-juegos";
}
```

**¿Qué hace?**
1. Valida y crea el juego
2. **Llama a `registrarPublicacionJuego()` ← AQUÍ SE COBRA EL 25€**

---

#### **PASO 3: Registrar la publicación (cobrar 25€)**

**Archivo:** `ServicioProveedor.java`

```java
// Constante definida al inicio de la clase
private static final BigDecimal COSTO_PUBLICACION = new BigDecimal("25.00");

@Override
public PublicacionJuego registrarPublicacionJuego(Juego juego, Usuario proveedor) {
    
    // Crear registro de publicación
    PublicacionJuego publicacion = new PublicacionJuego();
    publicacion.setJuego(juego);
    publicacion.setProveedor(proveedor);
    publicacion.setCosto(COSTO_PUBLICACION);  // 25€
    publicacion.setFechaPublicacion(LocalDateTime.now());
    
    // Guardar en BD (tabla: publicacion_juego)
    return publicacionJuegoRepository.save(publicacion);
}
```

**Resultado en BD:**
```sql
INSERT INTO publicacion_juego 
    (juego_id, proveedor_id, costo, fecha_publicacion)
VALUES 
    (789, 10, 25.00, '2025-11-21 11:00:00');
```

**¿Dónde va el dinero?**
- El proveedor **paga 25€** a la plataforma
- Se guarda en la tabla `publicacion_juego`
- La plataforma **gana 25€** (se suma después)

---

### Resumen del Flujo 2 (Publicación)

```
PROVEEDOR PUBLICA JUEGO

1. ProveedorController.publicarJuego()
   └─ Valida y crea el juego
   
2. ServicioProveedor.registrarPublicacionJuego()
   └─ Guarda publicación (25€) en tabla: publicacion_juego

RESULTADO:
✅ Juego visible en el catálogo
✅ Plataforma gana 25€
✅ Proveedor pagó 25€
```

---

## 💵 Flujo 3: Calcular Ganancias de la Plataforma

### Paso a Paso del Código

#### **PASO 1: Admin accede al dashboard**

**Archivo:** `AdminController.java`

```java
@GetMapping("/admin/ganancias")
public String verGanancias(Model model) {
    
    // CALCULAR GANANCIAS TOTALES
    ServicioAdministrador.ResumenGanancias resumen = 
        servicioAdministrador.calcularGananciasTotalesPlataforma();
    
    model.addAttribute("gananciasPorVentas", resumen.getGananciasPorVentas());
    model.addAttribute("gananciasPorPublicaciones", resumen.getGananciasPorPublicaciones());
    model.addAttribute("totalGanancias", resumen.getTotalGanancias());
    
    return "admin/ganancias";
}
```

**¿Qué hace?**
- Llama a `calcularGananciasTotalesPlataforma()` ← AQUÍ SE CALCULA TODO

---

#### **PASO 2: Calcular ganancias totales**

**Archivo:** `ServicioAdministrador.java`

```java
@Override
public ResumenGanancias calcularGananciasTotalesPlataforma() {
    
    // ===== PASO 2.1: GANANCIAS POR VENTAS (15%) =====
    BigDecimal gananciasPorVentas = calcularGananciasPorVentas();
    
    // ===== PASO 2.2: GANANCIAS POR PUBLICACIONES (25€) =====
    BigDecimal gananciasPorPublicaciones = calcularGananciasPorPublicaciones();
    
    // ===== PASO 2.3: SUMAR TODO =====
    BigDecimal totalGanancias = gananciasPorVentas.add(gananciasPorPublicaciones);
    
    // Contar ventas y publicaciones
    Long numeroVentas = servicioCompra.obtenerComprasValidas().size();
    Long numeroPublicaciones = servicioProveedor.contarPublicacionesPagadas();
    
    // Retornar resumen
    return new ResumenGanancias(
        gananciasPorVentas,
        gananciasPorPublicaciones,
        totalGanancias,
        numeroVentas,
        numeroPublicaciones
    );
}
```

**¿Qué hace?**
1. Calcula ganancias por ventas (15%)
2. Calcula ganancias por publicaciones (25€)
3. Suma todo

---

#### **PASO 2.1: Calcular ganancias por ventas (15%)**

**Archivo:** `ServicioAdministrador.java`

```java
@Override
public BigDecimal calcularGananciasPorVentas() {
    
    // PASO 2.1.1: Obtener total de todas las ventas
    BigDecimal totalVentas = servicioCompra.calcularTotalVentas();
    
    // PASO 2.1.2: Calcular 15% del total
    return servicioCompra.calcularComisionPlataforma(totalVentas);
}
```

---

**PASO 2.1.1: Obtener total de ventas**

**Archivo:** `ServicioCompra.java`

```java
@Override
public BigDecimal calcularTotalVentas() {
    
    // Obtener solo compras válidas (sin ADMIN)
    List<Compra> comprasValidas = obtenerComprasValidas();
    
    // Sumar todos los precios
    return comprasValidas.stream()
        .map(Compra::getPrecio)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**¿Qué hace?**
- Lee TODAS las compras de la tabla `compras`
- Excluye compras de ADMIN (no deberían existir, pero por seguridad)
- Suma todos los precios

**Ejemplo:**
```
Compras en BD:
- Compra 1: 50€
- Compra 2: 100€
- Compra 3: 75€
─────────────
Total: 225€
```

---

**PASO 2.1.2: Calcular 15% de comisión**

**Archivo:** `ServicioCompra.java`

```java
// Constante definida al inicio de la clase
private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.15");

@Override
public BigDecimal calcularComisionPlataforma(BigDecimal precio) {
    // Ejemplo: 225€ × 0.15 = 33.75€
    return precio.multiply(COMISION_PLATAFORMA)
                 .setScale(2, RoundingMode.HALF_UP);
}
```

**Ejemplo:**
```
Total ventas: 225€
Cálculo: 225 × 0.15 = 33.75€
Resultado: 33.75€ para la plataforma
```

---

#### **PASO 2.2: Calcular ganancias por publicaciones (25€)**

**Archivo:** `ServicioAdministrador.java`

```java
@Override
public BigDecimal calcularGananciasPorPublicaciones() {
    
    // PASO 2.2.1: Contar publicaciones
    Long numeroPublicaciones = servicioProveedor.contarPublicacionesPagadas();
    
    // PASO 2.2.2: Multiplicar por 25€
    return servicioProveedor.calcularIngresosPlataformaPorPublicaciones(numeroPublicaciones);
}
```

---

**PASO 2.2.1: Contar publicaciones**

**Archivo:** `ServicioProveedor.java`

```java
@Override
public Long contarPublicacionesPagadas() {
    // Contar registros en tabla: publicacion_juego
    return publicacionJuegoRepository.count();
}
```

**SQL equivalente:**
```sql
SELECT COUNT(*) FROM publicacion_juego;
-- Resultado: 8 publicaciones
```

---

**PASO 2.2.2: Multiplicar por 25€**

**Archivo:** `ServicioProveedor.java`

```java
private static final BigDecimal COSTO_PUBLICACION = new BigDecimal("25.00");

@Override
public BigDecimal calcularIngresosPlataformaPorPublicaciones(Long numeroPublicaciones) {
    // Ejemplo: 8 × 25€ = 200€
    return COSTO_PUBLICACION.multiply(new BigDecimal(numeroPublicaciones));
}
```

**Ejemplo:**
```
Número de publicaciones: 8
Cálculo: 8 × 25 = 200€
Resultado: 200€ para la plataforma
```

---

#### **PASO 2.3: Sumar todo**

**Archivo:** `ServicioAdministrador.java`

```java
BigDecimal totalGanancias = gananciasPorVentas.add(gananciasPorPublicaciones);
```

**Ejemplo:**
```
Ganancias por ventas (15%):      33.75€
Ganancias por publicaciones:    200.00€
─────────────────────────────────────
Total ganancias:                233.75€
```

---

### Resumen del Flujo 3 (Ganancias Plataforma)

```
CALCULAR GANANCIAS DE LA PLATAFORMA

1. AdminController.verGanancias()
   └─ Llama a ServicioAdministrador.calcularGananciasTotalesPlataforma()

2. ServicioAdministrador.calcularGananciasTotalesPlataforma()
   │
   ├─ calcularGananciasPorVentas()
   │  ├─ ServicioCompra.calcularTotalVentas()
   │  │  └─ SELECT SUM(precio) FROM compras → 225€
   │  │
   │  └─ ServicioCompra.calcularComisionPlataforma(225€)
   │     └─ 225 × 0.15 = 33.75€
   │
   ├─ calcularGananciasPorPublicaciones()
   │  ├─ ServicioProveedor.contarPublicacionesPagadas()
   │  │  └─ SELECT COUNT(*) FROM publicacion_juego → 8
   │  │
   │  └─ ServicioProveedor.calcularIngresosPlataformaPorPublicaciones(8)
   │     └─ 8 × 25 = 200€
   │
   └─ SUMAR: 33.75€ + 200€ = 233.75€

RESULTADO:
✅ Ganancias por ventas: 33.75€
✅ Ganancias por publicaciones: 200.00€
✅ Total ganancias: 233.75€
```

---

## 💰 Flujo 4: Calcular Ingresos del Proveedor

### Paso a Paso del Código

#### **PASO 1: Proveedor accede a su panel**

**Archivo:** `ProveedorController.java`

```java
@GetMapping("/proveedor/ventas")
public String verVentas(Principal principal, Model model) {
    
    // Obtener proveedor
    Usuario proveedor = servicioUsuario.buscarUsuarioPorEmail(principal.getName());
    
    // OBTENER MOVIMIENTOS FINANCIEROS
    List<MovimientoProveedor> movimientos = 
        servicioProveedor.obtenerMovimientosDeProveedor(proveedor);
    
    // CALCULAR INGRESOS PENDIENTES
    BigDecimal pendiente = 
        servicioProveedor.calcularIngresosPendientes(proveedor);
    
    model.addAttribute("movimientos", movimientos);
    model.addAttribute("pendiente", pendiente);
    
    return "proveedor/ventas";
}
```

**¿Qué hace?**
- Obtiene todos los movimientos del proveedor
- Calcula cuánto dinero tiene pendiente de cobro

---

#### **PASO 2: Obtener movimientos del proveedor**

**Archivo:** `ServicioProveedor.java`

```java
@Override
public List<MovimientoProveedor> obtenerMovimientosDeProveedor(Usuario proveedor) {
    
    // Buscar en BD todos los movimientos de este proveedor
    return movimientoProveedorRepository
        .findByProveedorOrderByFechaDesc(proveedor);
}
```

**SQL equivalente:**
```sql
SELECT * 
FROM movimiento_proveedor 
WHERE proveedor_id = 10 
ORDER BY fecha DESC;
```

**Resultado:**
```
Movimientos del proveedor ID 10:
1. Compra #456 - 85.00€ - 2025-11-21 - No cobrado
2. Compra #457 - 42.50€ - 2025-11-20 - No cobrado
3. Compra #458 - 68.00€ - 2025-11-19 - Cobrado
```

---

#### **PASO 3: Calcular ingresos pendientes**

**Archivo:** `ServicioProveedor.java`

```java
@Override
public BigDecimal calcularIngresosPendientes(Usuario proveedor) {
    
    // Obtener solo movimientos NO cobrados
    List<MovimientoProveedor> movimientosPendientes = 
        movimientoProveedorRepository.findByProveedorAndCobradoFalse(proveedor);
    
    // Sumar todos los montos
    return movimientosPendientes.stream()
        .map(MovimientoProveedor::getMonto)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

**SQL equivalente:**
```sql
SELECT SUM(monto) 
FROM movimiento_proveedor 
WHERE proveedor_id = 10 AND cobrado = false;
```

**Ejemplo:**
```
Movimientos pendientes:
- Movimiento 1: 85.00€
- Movimiento 2: 42.50€
─────────────────────
Total pendiente: 127.50€
```

---

### Resumen del Flujo 4 (Ingresos Proveedor)

```
CALCULAR INGRESOS DEL PROVEEDOR

1. ProveedorController.verVentas()
   └─ Llama a ServicioProveedor.calcularIngresosPendientes(proveedor)

2. ServicioProveedor.calcularIngresosPendientes()
   └─ SELECT SUM(monto) 
      FROM movimiento_proveedor 
      WHERE proveedor_id = 10 AND cobrado = false
      → Resultado: 127.50€

RESULTADO:
✅ Total pendiente de cobro: 127.50€
```

---

## 📊 Clases y Métodos Involucrados

### 1. CompraController

**Responsabilidad:** Recibir peticiones de compra.

**Métodos clave:**
- `comprarJuego()` - Punto de entrada para compras

---

### 2. ServicioCompra

**Responsabilidad:** Gestionar compras y calcular comisiones.

**Métodos clave:**
- `crearNuevaCompra()` - Método principal que orquesta todo
- `calcularMontoParaProveedor()` - Calcula 85%
- `calcularComisionPlataforma()` - Calcula 15%
- `calcularTotalVentas()` - Suma todas las ventas
- `obtenerComprasValidas()` - Filtra compras válidas

**Constantes:**
```java
private static final BigDecimal COMISION_PROVEEDOR = new BigDecimal("0.85");
private static final BigDecimal COMISION_PLATAFORMA = new BigDecimal("0.15");
```

---

### 3. ServicioProveedor

**Responsabilidad:** Gestionar movimientos financieros y publicaciones.

**Métodos clave:**
- `crearMovimientoProveedor()` - Crea registro de ingreso para proveedor
- `registrarPublicacionJuego()` - Registra publicación (cobra 25€)
- `calcularIngresosPendientes()` - Suma ingresos no cobrados
- `contarPublicacionesPagadas()` - Cuenta publicaciones
- `calcularIngresosPlataformaPorPublicaciones()` - Multiplica por 25€

**Constantes:**
```java
private static final BigDecimal COSTO_PUBLICACION = new BigDecimal("25.00");
```

---

### 4. ServicioAdministrador

**Responsabilidad:** Calcular ganancias de la plataforma.

**Métodos clave:**
- `calcularGananciasTotalesPlataforma()` - Método principal
- `calcularGananciasPorVentas()` - Calcula 15% de ventas
- `calcularGananciasPorPublicaciones()` - Calcula ingresos por publicaciones

---

### 5. ServicioBiblioteca

**Responsabilidad:** Gestionar biblioteca de usuarios.

**Métodos clave:**
- `agregarJuegoABiblioteca()` - Añade juego comprado a biblioteca

---

### 6. Entidades (Modelos)

#### Compra
```java
public class Compra {
    private Long id;
    private Usuario usuario;        // Quién compró
    private Juego juego;            // Qué compró
    private BigDecimal precio;      // Cuánto pagó
    private LocalDateTime fechaCompra;
    private Boolean pagadaAlProveedor; // false = pendiente
}
```

#### MovimientoProveedor
```java
public class MovimientoProveedor {
    private Long id;
    private Usuario proveedor;      // Quién recibe
    private Compra compra;          // De qué compra viene
    private BigDecimal monto;       // Cuánto recibe (85%)
    private LocalDateTime fecha;
    private Boolean cobrado;        // false = pendiente
}
```

#### PublicacionJuego
```java
public class PublicacionJuego {
    private Long id;
    private Juego juego;            // Juego publicado
    private Usuario proveedor;      // Quién publicó
    private BigDecimal costo;       // 25€
    private LocalDateTime fechaPublicacion;
}
```

---

## 🔄 Flujo Completo Resumido

### Ejemplo Real Paso a Paso

```
ESCENARIO:
- Cliente compra juego de 100€
- Proveedor que publicó el juego cobra 85%
- Plataforma se queda con 15%

════════════════════════════════════════════════════════════

PASO 1: CLIENTE HACE CLIC EN "COMPRAR"
────────────────────────────────────────
Vista HTML → POST /comprar/123

PASO 2: CONTROLADOR RECIBE PETICIÓN
────────────────────────────────────────
CompraController.comprarJuego()
├─ Busca usuario: SELECT * FROM usuarios WHERE email = ?
├─ Busca juego: SELECT * FROM juegos WHERE id = 123
├─ Valida: ¿Puede comprar? → SÍ
├─ Valida: ¿Ya lo compró? → NO
└─ Llama a ServicioCompra.crearNuevaCompra()

PASO 3: CREAR COMPRA (AQUÍ PASA TODO)
────────────────────────────────────────
ServicioCompra.crearNuevaCompra()

  3.1. Crear registro de compra
       INSERT INTO compras (usuario_id, juego_id, precio, fecha_compra, pagada_al_proveedor)
       VALUES (5, 123, 100.00, NOW(), false)
       
  3.2. Añadir a biblioteca
       ServicioBiblioteca.agregarJuegoABiblioteca()
       INSERT INTO biblioteca_usuario (usuario_id, juego_id, fecha_adquisicion)
       VALUES (5, 123, NOW())
       
  3.3. Calcular 85% para proveedor
       ServicioCompra.calcularMontoParaProveedor(100€)
       Resultado: 100 × 0.85 = 85€
       
  3.4. Crear movimiento para proveedor
       ServicioProveedor.crearMovimientoProveedor(proveedor, compra, 85€)
       INSERT INTO movimiento_proveedor (proveedor_id, compra_id, monto, fecha, cobrado)
       VALUES (10, 456, 85.00, NOW(), false)
       
  3.5. El 15% queda para la plataforma
       No se guarda, se calcula después:
       100 × 0.15 = 15€

PASO 4: RESULTADO
────────────────────────────────────────
✅ Cliente: Tiene el juego en su biblioteca
✅ Proveedor: Tiene 85€ pendientes de cobro
✅ Plataforma: Tiene 15€ de ganancia

════════════════════════════════════════════════════════════

DESPUÉS: ADMIN CONSULTA GANANCIAS
────────────────────────────────────────
AdminController.verGanancias()
└─ ServicioAdministrador.calcularGananciasTotalesPlataforma()
   
   Ganancias por ventas:
   ├─ SELECT SUM(precio) FROM compras → 500€ (5 compras)
   └─ 500 × 0.15 = 75€
   
   Ganancias por publicaciones:
   ├─ SELECT COUNT(*) FROM publicacion_juego → 3
   └─ 3 × 25 = 75€
   
   Total: 75€ + 75€ = 150€

════════════════════════════════════════════════════════════

DESPUÉS: PROVEEDOR CONSULTA SUS VENTAS
────────────────────────────────────────
ProveedorController.verVentas()
└─ ServicioProveedor.calcularIngresosPendientes(proveedor)
   
   SELECT SUM(monto) 
   FROM movimiento_proveedor 
   WHERE proveedor_id = 10 AND cobrado = false
   
   Resultado: 255€ pendientes
```

---

## 📝 Resumen Final

### ¿Cómo se calcula el 15%?

**Método:** `ServicioCompra.calcularComisionPlataforma(precio)`

**Código:**
```java
return precio.multiply(new BigDecimal("0.15"));
```

**Cuándo se usa:**
- Al calcular ganancias totales de la plataforma
- No se guarda en BD, se calcula sobre la marcha

---

### ¿Cómo se calcula el 85%?

**Método:** `ServicioCompra.calcularMontoParaProveedor(precio)`

**Código:**
```java
return precio.multiply(new BigDecimal("0.85"));
```

**Cuándo se usa:**
- Al crear una compra
- Se guarda en `movimiento_proveedor`

---

### ¿Cómo se cobra el 25€?

**Método:** `ServicioProveedor.registrarPublicacionJuego(juego, proveedor)`

**Código:**
```java
publicacion.setCosto(new BigDecimal("25.00"));
return publicacionJuegoRepository.save(publicacion);
```

**Cuándo se usa:**
- Al publicar un juego
- Se guarda en `publicacion_juego`

---

### Tablas de Base de Datos Involucradas

1. **compras** - Registra todas las compras (precio completo)
2. **movimiento_proveedor** - Registra ingresos de proveedores (85%)
3. **publicacion_juego** - Registra publicaciones (25€)
4. **biblioteca_usuario** - Juegos en biblioteca de usuarios

---

### Flujo del Dinero en Resumen

```
PUBLICACIÓN:
Proveedor → paga 25€ → Plataforma
(se guarda en: publicacion_juego)

COMPRA:
Cliente → paga 100€ → Sistema
├─ 85€ → Proveedor (se guarda en: movimiento_proveedor)
└─ 15€ → Plataforma (se calcula después sumando todas las compras)

GANANCIAS PLATAFORMA:
= (Total de todas las compras × 15%) + (Número de publicaciones × 25€)
```

---

**¡Ahora entiendes EXACTAMENTE cómo funciona el flujo de dinero en el código!** 💰
