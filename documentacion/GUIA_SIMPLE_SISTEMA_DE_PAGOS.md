# 💰 GUÍA SIMPLE: SISTEMA DE PAGOS DE PIXELSHOP

## 🎯 ¿QUÉ ES PIXELSHOP?

PixelShop es como Steam o Epic Games: una tienda online donde los desarrolladores publican sus juegos y los usuarios los compran.

---

## 💡 ¿CÓMO GANA DINERO LA PLATAFORMA?

La plataforma gana dinero de **2 formas**:

### 1. Cobrar por publicar juegos: **25€**
- Cuando un desarrollador sube un juego nuevo → paga **25€**

### 2. Comisión por cada venta: **15%**
- Cuando alguien compra un juego:
  - **85%** va para el desarrollador
  - **15%** se queda la plataforma

### Ejemplo:
Si un juego cuesta **100€**:
- Usuario paga → **100€**
- Desarrollador recibe → **85€**
- Plataforma recibe → **15€**

Además, el desarrollador pagó **25€** cuando publicó el juego.

---

## 🏗️ PASO 1: CREAR LAS TABLAS EN LA BASE DE DATOS

Necesitamos 3 tablas principales para guardar la información del dinero:

### Tabla 1: `publicacion_juego` (Registro de publicaciones)
Guarda cuando un desarrollador publica un juego.

```sql
- id
- juego_id
- proveedor_id  (quién publicó el juego)
- fecha_publicacion
- pagado (true = ya pagó los 25€)
```

📍 **Archivo:** `src/main/java/com/example/model/PublicacionJuego.java`

```java
@Column(nullable = false)
private Boolean pagado = false; // 25€
```

### Tabla 2: `compras` (Registro de compras)
Guarda cada vez que un usuario compra un juego.

```sql
- id
- usuario_id  (quién compró)
- juego_id    (qué compró)
- precio_pagado  (cuánto pagó)
- fecha_compra
- pagado_al_proveedor (si ya se le pagó al desarrollador)
```

📍 **Archivo:** `src/main/java/com/example/model/Compra.java`

```java
@Column(name = "precio_pagado")
private BigDecimal precioPagado;  // Guarda el 100% del precio
```

### Tabla 3: `movimiento_proveedor` (Pagos pendientes a desarrolladores)
Guarda cuánto dinero debe recibir cada desarrollador.

```sql
- id
- proveedor_id  (a quién se le debe pagar)
- compra_id
- monto_neto    (85% del precio)
- pagado        (si ya cobró)
- fecha
```

📍 **Archivo:** `src/main/java/com/example/model/MovimientoProveedor.java`

```java
@Column(name = "monto_neto")
private BigDecimal montoNeto; // 85% del precio
```

---

## 📝 PASO 2: CUANDO UN DESARROLLADOR PUBLICA UN JUEGO

### ¿Qué pasa cuando alguien publica un juego?

1. El desarrollador sube las imágenes y datos del juego
2. Se guarda el juego en la base de datos
3. **Se crea un registro en `publicacion_juego` con `pagado=true`** ← Aquí se cobra los 25€

📍 **Archivo:** `src/main/java/com/example/controller/ProveedorController.java`  
📍 **Método:** `publicarJuego()`  
📍 **Líneas:** 139-146

```java
// Registrar PUBLICACION (cobro de 25€)
PublicacionJuego publicacion = new PublicacionJuego();
publicacion.setJuego(juego);
publicacion.setProveedor(proveedor);
publicacion.setFechaPublicacion(LocalDateTime.now());
publicacion.setPagado(true); // ← AQUÍ se marca que pagó 25€
publicacionRepository.save(publicacion);
```

### ¿Por qué `pagado=true`?
Porque en este proyecto se simula que el pago se hace automáticamente. En la vida real, aquí habría una integración con PayPal o Stripe.

---

## 🛒 PASO 3: CUANDO UN USUARIO COMPRA UN JUEGO

Esto es lo más importante. Cuando alguien compra un juego pasan **4 cosas**:

### Cosa 1: Crear el registro de compra

Se guarda que "Usuario X compró Juego Y por Z euros".

📍 **Archivo:** `src/main/java/com/example/controller/CompraController.java`  
📍 **Líneas:** 51-58

```java
Compra compra = new Compra();
compra.setUsuario(usuario);           // Quién compró
compra.setJuego(juego);               // Qué compró
compra.setPrecioPagado(juego.getPrecio());  // Cuánto pagó (100%)
compra.setFechaCompra(LocalDateTime.now());
compra.setPagadoAlProveedor(false);   // Todavía no se le pagó al dev
compraRepository.save(compra);
```

### Cosa 2: Añadir el juego a la biblioteca del usuario

El usuario ahora tiene el juego en su cuenta.

📍 **Líneas:** 60-65

```java
BibliotecaUsuario biblioteca = new BibliotecaUsuario();
biblioteca.setUsuario(usuario);
biblioteca.setJuego(juego);
biblioteca.setFechaAdquisicion(LocalDateTime.now());
bibliotecaRepository.save(biblioteca);
```

### Cosa 3: **CALCULAR EL 85% PARA EL DESARROLLADOR** ⭐

Esta es la parte más importante del sistema de pagos.

📍 **Líneas:** 67-75

```java
// AQUÍ SE CALCULA LA COMISIÓN
BigDecimal montoProveedor = juego.getPrecio().multiply(new BigDecimal("0.85"));

MovimientoProveedor movimiento = new MovimientoProveedor();
movimiento.setProveedor(juego.getProveedor());  // A quién se le debe pagar
movimiento.setCompra(compra);
movimiento.setMontoNeto(montoProveedor);  // ← GUARDA EL 85%
movimiento.setPagado(false);              // Todavía no cobró
movimiento.setFecha(LocalDateTime.now());
movimientoRepository.save(movimiento);
```

### ¿Qué hace `multiply(new BigDecimal("0.85"))`?

Es una multiplicación simple:
- Si el juego cuesta **100€**
- `100 × 0.85 = 85€` ← Esto es lo que recibe el desarrollador
- Los **15€** restantes se quedan en la plataforma (no se guardan explícitamente, es la diferencia)

### Cosa 4: El usuario recibe un mensaje de éxito

📍 **Línea:** 77

```java
redirectAttributes.addFlashAttribute("success", "¡Compra realizada! El juego está en tu biblioteca");
```

---

## 📊 PASO 4: VER LAS GANANCIAS (PANEL DE ADMIN)

Los administradores pueden ver cuánto dinero ha ganado la plataforma.

📍 **Archivo:** `src/main/java/com/example/controller/AdminController.java`  
📍 **Método:** `ganancias()`  
📍 **Líneas:** 36-54

### ¿Cómo se calcula?

```java
// 1. Sumar todo lo que pagaron los usuarios
BigDecimal totalVentas = compraRepository.calcularTotalVentas();

// 2. Calcular el 15% de eso (nuestra comisión)
BigDecimal gananciaVentas = totalVentas.multiply(new BigDecimal("0.15"));

// 3. Contar cuántos juegos se publicaron
Long totalPublicaciones = publicacionRepository.contarPublicacionesPagadas();

// 4. Multiplicar por 25€ cada uno
BigDecimal gananciaPublicaciones = new BigDecimal(totalPublicaciones).multiply(new BigDecimal("25"));

// 5. SUMAR TODO
BigDecimal gananciaTotal = gananciaVentas.add(gananciaPublicaciones);
```

### Ejemplo con números reales:

Supongamos:
- Se han vendido juegos por un total de **1000€**
- Se han publicado **10 juegos**

Cálculo:
- Comisión de ventas: `1000€ × 0.15 = 150€`
- Tarifa de publicación: `10 × 25€ = 250€`
- **Total ganado por la plataforma: 150€ + 250€ = 400€**

---

## 💼 PASO 5: PANEL DEL DESARROLLADOR (VER SUS VENTAS)

Los desarrolladores pueden ver cuánto dinero tienen pendiente de cobrar.

📍 **Archivo:** `src/main/java/com/example/controller/ProveedorController.java`  
📍 **Método:** `ventas()`  
📍 **Líneas:** 254-264

```java
// Buscar todos los movimientos de este desarrollador
List<MovimientoProveedor> movimientos = movimientoRepository.findByProveedorOrderByFechaDesc(proveedor);

// Sumar solo los que NO ha cobrado todavía
BigDecimal ingresosPendientes = movimientoRepository.calcularIngresosPendientes(proveedor);
```

### ¿Qué ve el desarrollador?

Una lista con:
- Fecha de la venta
- Juego vendido
- Monto a recibir (85%)
- Si ya lo cobró o no

### ¿Cómo cobra el desarrollador?

Hay un botón "Cobrar" que marca el movimiento como pagado.

📍 **Método:** `cobrarMovimiento()`  
📍 **Líneas:** 266-274

```java
MovimientoProveedor movimiento = movimientoRepository.findById(movimientoId).get();
movimiento.setPagado(true);  // ← Marca que ya cobró
movimientoRepository.save(movimiento);
```

---

## 🔍 VALIDACIONES IMPORTANTES

### 1. Los administradores NO pueden comprar juegos

¿Por qué? Para evitar que se manipulen las estadísticas.

📍 **Archivo:** `CompraController.java`  
📍 **Líneas:** 36-40

```java
if (usuario.getRol() == Rol.ADMIN) {
    redirectAttributes.addFlashAttribute("error", "Los administradores no pueden comprar juegos");
    return "redirect:/juego/" + juegoId;
}
```

### 2. No puedes comprar el mismo juego dos veces

📍 **Líneas:** 46-49

```java
if (compraRepository.existsByUsuarioAndJuego(usuario, juego)) {
    redirectAttributes.addFlashAttribute("error", "Ya tienes este juego en tu biblioteca");
    return "redirect:/juego/" + juegoId;
}
```

---

## 📈 CONSULTAS A LA BASE DE DATOS

Para hacer los cálculos, necesitamos hacer consultas SQL:

### Consulta 1: Sumar todas las ventas

📍 **Archivo:** `src/main/java/com/example/repository/CompraRepository.java`

```java
@Query("SELECT SUM(c.precioPagado) FROM Compra c")
BigDecimal calcularTotalVentas();
```

Esto suma todos los valores de la columna `precio_pagado` de la tabla `compras`.

### Consulta 2: Sumar ingresos pendientes de un desarrollador

📍 **Archivo:** `src/main/java/com/example/repository/MovimientoProveedorRepository.java`

```java
@Query("SELECT SUM(m.montoNeto) FROM MovimientoProveedor m 
       WHERE m.proveedor = :proveedor AND m.pagado = false")
BigDecimal calcularIngresosPendientes(@Param("proveedor") Usuario proveedor);
```

Esto suma todos los `monto_neto` (85% de cada venta) que todavía no han sido cobrados.

### Consulta 3: Contar cuántos juegos se han publicado

📍 **Archivo:** `src/main/java/com/example/repository/PublicacionJuegoRepository.java`

```java
@Query("SELECT COUNT(p) FROM PublicacionJuego p WHERE p.pagado = true")
Long contarPublicacionesPagadas();
```

Cuenta cuántos registros hay en la tabla `publicacion_juego` con `pagado=true`.

---

## 🎬 RESUMEN: EL FLUJO COMPLETO EN 5 PASOS

### PASO 1: Desarrollador publica juego
```
Desarrollador → Sube juego → Se guarda en BD → publicacion_juego.pagado = true
RESULTADO: Plataforma gana 25€
```

### PASO 2: Usuario compra juego
```
Usuario → Compra juego de 100€ → Se guarda en tabla 'compras' con precio_pagado=100€
```

### PASO 3: Se calcula la comisión
```
100€ × 0.85 = 85€ → Se guarda en 'movimiento_proveedor' con monto_neto=85€
RESULTADO: 
- Desarrollador tiene pendiente 85€
- Plataforma gana 15€ (diferencia implícita)
```

### PASO 4: Juego se añade a la biblioteca del usuario
```
Se guarda en 'biblioteca_usuario' → Usuario puede ver/descargar el juego
```

### PASO 5: Desarrollador cobra su dinero
```
Desarrollador → Ve panel de ventas → Botón "Cobrar" → movimiento.pagado = true
RESULTADO: Desarrollador recibe sus 85€
```

---

## 🧮 FÓRMULAS SIMPLES

### Ganancia de la plataforma por comisión:
```
Total ventas × 0.15 = Ganancia por ventas
```

### Ganancia de la plataforma por publicaciones:
```
Número de juegos publicados × 25€ = Ganancia por publicaciones
```

### Ganancia total de la plataforma:
```
Ganancia por ventas + Ganancia por publicaciones = TOTAL
```

### Dinero para el desarrollador:
```
Precio del juego × 0.85 = Lo que recibe el desarrollador
```

---

## 📂 ARCHIVOS CLAVE DEL PROYECTO

### Modelos (Estructura de las tablas):
- `PublicacionJuego.java` → Tabla de publicaciones
- `Compra.java` → Tabla de compras
- `MovimientoProveedor.java` → Tabla de pagos a desarrolladores
- `Juego.java` → Tabla de juegos

### Controladores (Lógica de negocio):
- `CompraController.java` → Gestiona las compras
- `ProveedorController.java` → Gestiona publicaciones y ventas
- `AdminController.java` → Panel de administración

### Repositorios (Consultas a BD):
- `CompraRepository.java` → Consultas de compras
- `MovimientoProveedorRepository.java` → Consultas de movimientos
- `PublicacionJuegoRepository.java` → Consultas de publicaciones

---

## ✅ CHECKLIST: ¿QUÉ SE NECESITA PARA QUE FUNCIONE?

- [ ] Base de datos MySQL configurada
- [ ] Tablas creadas (JPA las crea automáticamente)
- [ ] Spring Boot funcionando
- [ ] 3 tipos de usuarios: ADMIN, PROVEEDOR, USUARIO
- [ ] Sistema de login funcional
- [ ] Formulario para publicar juegos
- [ ] Catálogo de juegos visible
- [ ] Botón de compra en cada juego
- [ ] Panel de ventas para desarrolladores
- [ ] Panel de ganancias para administradores

---

## 🎯 CONCLUSIÓN SIMPLE

Este sistema hace 3 cosas principales:

1. **Cobra 25€** cuando alguien publica un juego
2. **Divide el dinero** de cada venta: 85% desarrollador, 15% plataforma
3. **Guarda todo** en la base de datos para que se pueda consultar después

La parte más importante está en el método `comprarJuego()` de `CompraController.java`, específicamente en la línea:

```java
BigDecimal montoProveedor = juego.getPrecio().multiply(new BigDecimal("0.85"));
```

Esa línea calcula cuánto recibe el desarrollador. El resto es gestión de base de datos y mostrar la información en pantallas.

---

**¿Dudas? Busca estas palabras clave en el código:**
- `0.85` → Cálculo del 85%
- `0.15` → Cálculo del 15%
- `25` → Tarifa de publicación
- `MovimientoProveedor` → Pagos a desarrolladores
- `calcularTotalVentas()` → Suma de todas las ventas
